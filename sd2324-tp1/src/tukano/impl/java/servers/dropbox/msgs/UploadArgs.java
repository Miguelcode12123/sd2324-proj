package tukano.impl.java.servers.dropbox.msgs;

public record UploadArgs(String path, String mode, boolean autorename, boolean mute, boolean strict_conflict) {
}