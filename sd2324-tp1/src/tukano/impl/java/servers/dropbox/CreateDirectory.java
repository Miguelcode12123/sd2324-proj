package tukano.impl.java.servers.dropbox;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tukano.impl.java.servers.dropbox.msgs.CreateFolderV2Args;

/**
 * The argument and return classes must map to the specifications of the tukano.impl.java.servers.dropbox
 * api
 */

public class CreateDirectory {

	private static final String apiKey = "cwendmo115gd0t1";
	private static final String apiSecret = "3m4ddrj3nci3uv9";
	private static final String accessTokenStr = "sl.B2HcR_fzZyzo4AzPv-5WUWKgj0F84IGaebQvFchUhAGgBdsaEPmUd4qUi1mpZP813RApodvnwwZRJcgG2ynd7GIqfcLniz12eqb32oDdm0JNS_Xqy8Ca0AZOYwKDnUgR6Q2M-AkMqQqn";

	private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";

	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

	public CreateDirectory() {
		// initialize the service with the auth information. Always the same.
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
	}

	public void execute(String directoryName) throws Exception {
		// create a request object
		var createFolder = new OAuthRequest(Verb.POST, CREATE_FOLDER_V2_URL);
		// add header
		createFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		// the body of the request, corresponds to the "parameters" in the tukano.impl.java.servers.dropbox api
		createFolder.setPayload(json.toJson(new CreateFolderV2Args(directoryName, false)));
		// authenticate the request, i.e. use our private key, in order for the tukano.impl.java.servers.dropbox
		// api to know who we are. Correspondes to the header parameter "Authorization",
		// in the tukano.impl.java.servers.dropbox api
		service.signRequest(accessToken, createFolder);
		// send the request to the service
		Response r = service.execute(createFolder);
		// error processing
		if (r.getCode() != HTTP_SUCCESS)
			throw new RuntimeException(String.format("Failed to create directory: %s, Status: %d, \nReason: %s\n",
					directoryName, r.getCode(), r.getBody()));
	}

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("usage: java CreateDirectory <dir>");
			System.exit(0);
		}

		var directory = args[0];
		var cd = new CreateDirectory();

		cd.execute(directory);
		System.out.println("Directory '" + directory + "' created successfuly.");
	}

}
