package tukano.impl.java.servers;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import tukano.impl.java.servers.dropbox.msgs.CreateFolderV2Args;
import tukano.impl.java.servers.dropbox.msgs.DownloadArgs;
import tukano.impl.java.servers.dropbox.msgs.UploadArgs;
import org.pac4j.scribe.builder.api.DropboxApi20;
import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedBlobs;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import tukano.impl.java.clients.Clients;
import utils.Hash;
import utils.Hex;
import utils.IO;

import java.io.File;
import java.util.logging.Logger;

import static java.lang.String.format;
import static tukano.api.java.Result.ErrorCode.*;
import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

public class JavaBlobsDropbox implements ExtendedBlobs {

    private static final String apiKey = "cwendmo115gd0t1";
    private static final String apiSecret = "3m4ddrj3nci3uv9";
    private static final String accessTokenStr = "sl.B2EayIP8AWWC02POf7H4mcuLeNMDaJTahUoFPNPEZD6508wEKcHvLDKL5lsJ1xy--lYxBeXZW-Ag-rpxuQ5lfd3MmYJuq5AzU_4GLlGmzPf2E2yGtGNnvBYlDVNMu5kt5rP_nE2w0bZz";
    private static final String UPLOAD_URL = "https://content.dropboxapi.com/2/files/upload";
    private static final String DOWNLOAD_URL = "https://content.dropboxapi.com/2/files/download";
    private static final String DELETE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";
    private static final int HTTP_SUCCESS = 200;
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String DROPBOX_API_ARG = "Dropbox-API-Arg";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String OCTET_CONTENT_TYPE = "application/octet-stream";
    private static final String BLOBS_ROOT_DIR = "/tmp/blobs/";
    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    private static Logger Log = Logger.getLogger(JavaBlobsDropbox.class.getName());
    public JavaBlobsDropbox() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Log.info(() -> format("upload : blobId = %s, sha256 = %s\n", blobId, Hex.of(Hash.sha256(bytes))));

        if (!validBlobId(blobId))
            return error(FORBIDDEN);

        var file = toFilePath(blobId);
        if (file == null)
            return error(BAD_REQUEST);

        try {
            String directoryName = file.getAbsolutePath();
            var upload = new OAuthRequest(Verb.POST, UPLOAD_URL);

            upload.addHeader(DROPBOX_API_ARG, json.toJson(new UploadArgs(directoryName, "overwrite", false, false, false)));
            upload.addHeader(CONTENT_TYPE_HDR, OCTET_CONTENT_TYPE);
            upload.setPayload(bytes);

            service.signRequest(accessToken, upload);
            // send the request to the service
            Response r = service.execute(upload);
            // error processing
            if (r.getCode() != HTTP_SUCCESS)
                throw new RuntimeException(String.format("Failed to upload file: %s, Status: %d, \nReason: %s\n",
                        directoryName, r.getCode(), r.getBody()));
            return ok();
        } catch (Exception e) {
            return error(INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Log.info(() -> format("download : blobId = %s\n", blobId));

        var file = toFilePath(blobId);
        if (file == null)
            return error(BAD_REQUEST);
        try {
            String directoryName = file.getAbsolutePath();
            var download = new OAuthRequest(Verb.POST, DOWNLOAD_URL);

            download.addHeader(DROPBOX_API_ARG, json.toJson(new DownloadArgs(directoryName)));
            download.addHeader(CONTENT_TYPE_HDR, OCTET_CONTENT_TYPE);

            service.signRequest(accessToken, download);
            // send the request to the service
            Response r = service.execute(download);
            // error processing
            if (r.getCode() != HTTP_SUCCESS)
                throw new RuntimeException(String.format("Failed to download file: %s, Status: %d, \nReason: %s\n",
                        directoryName, r.getCode(), r.getBody()));
            return ok(r.getStream().readAllBytes());
        } catch (Exception e) {
            return error(INTERNAL_ERROR);
        }

    }


    @Override
    public Result<Void> delete(String blobId, String token) {
        return null;
    }

    @Override
    public Result<Void> deleteAllBlobs(String userId, String token) {
        return null;
    }

    private boolean validBlobId(String blobId) {
        return Clients.ShortsClients.get().getShort(blobId).isOK();
    }

    private File toFilePath(String blobId) {
        var parts = blobId.split("-");
        if (parts.length != 2)
            return null;

        var res = new File(BLOBS_ROOT_DIR + parts[0] + "/" + parts[1]);
        try {
            createDirectory(res.getParentFile().getAbsolutePath());

            return res;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create directory");
        }
    }

    private void createDirectory (String directoryName) throws Exception {
        // create a request object
        var createFolder = new OAuthRequest(Verb.POST, CREATE_FOLDER_V2_URL);
        // add header
        createFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
        // the body of the request, corresponds to the "parameters" in the tukano.impl.java.servers.dropbox api
        createFolder.setPayload(json.toJson(new CreateFolderV2Args(directoryName, false)));
        // authenticate the request, i.e. use our private key, in order for the tukano.impl.java.servers.dropbox
        // api to know who we are. Corresponds to the header parameter "Authorization",
        // in the tukano.impl.java.servers.dropbox api
        service.signRequest(accessToken, createFolder);
        // send the request to the service
        Response r = service.execute(createFolder);
        // error processing
        if (r.getCode() != HTTP_SUCCESS && r.getCode() != 409)
            throw new RuntimeException(String.format("Failed to create directory: %s, Status: %d, \nReason: %s\n",
                    directoryName, r.getCode(), r.getBody()));
    }
}
