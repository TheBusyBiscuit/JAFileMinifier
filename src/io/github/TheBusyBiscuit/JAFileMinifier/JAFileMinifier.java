package io.github.TheBusyBiscuit.JAFileMinifier;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JAFileMinifier {
	
	public static void main(String[] args) throws InterruptedException {
		if (args.length < 1) {
			throw new IllegalArgumentException("Usage: <manual/auto>");
		}
		
		if (!CompilerMode.isValid(args[0].toUpperCase())) {
			throw new IllegalArgumentException("Usage: <manual/auto>");
		}
		
		CompilerMode mode = CompilerMode.valueOf(args[0].toUpperCase());

		switch (mode) {
		case AUTO: {
			while (true) {
				run();
				Thread.sleep(3000);
			}
		}
		case MANUAL:{
			run();
			break;
		}
		default:
			break;
		}
	}
	
	private static void run() {
		int files = run(new File(""));
		
		if (files > 0) {
			if (files == 1)
				System.out.println(" FINISHED (Minified " + files + " file)");
			else 
				System.out.println(" FINISHED (Minified " + files + " files)");
		}
	}

	private static int run(File directory) {
		int files = 0;
		
		for (File file: directory.listFiles()) {
			if (file.isDirectory()) {
				files += run(file);
			}
			else {
				for (FileExtension ext: FileExtension.values()) {
					if (file.getName().endsWith(ext.file) && !file.getName().endsWith(".min" + ext.file)) {
						try {
							if (handleFileExtension(file, ext)) {
								files++;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		return files;
	}

	private static boolean handleFileExtension(File file, FileExtension ext) throws IOException {
		String destination = file.getParent() + "/" + file.getName().replace(ext.file, ".min" + ext.file);
		File minified = new File(destination);
		
		if (minified.exists()) {
			if (minified.lastModified() < file.lastModified()) {
				System.out.println("  " + file.getName().replace(ext.file, ".min" + ext.file) + " appears to be outdated!");
			}
			else {
				return false;
			}
		}

		URL url = new URL(ext.url);
		System.out.println("   Type: " + ext.type);
		
		byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
		bytes = ("input=" + URLEncoder.encode(new String(bytes), "UTF-8")).getBytes("UTF-8");

		System.out.println("   Connecting... '" + url.toString() + "'");
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("User-Agent", "JAFileMinifier (by TheBusyBiscuit)");
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
		connection.setDoOutput(true);
		connection.setConnectTimeout(7000);

		System.out.println("   Sending data... (" + bytes.length + " Bytes)");
		DataOutputStream output = new DataOutputStream(connection.getOutputStream());
		output.write(bytes);

		final int code = connection.getResponseCode();

		System.out.println("   Response: " + code);

		if (code == 200) {
			System.out.println("   Exporting '" + destination + "'");
		    ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
			FileOutputStream fos = new FileOutputStream(destination);
		    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		    fos.close();
		    connection.disconnect();
			System.out.println("  Done!");
			return true;
		}
		else {
		    System.err.println("  Could not connect to Webservice.");
		    return false;
		}
	}

}
