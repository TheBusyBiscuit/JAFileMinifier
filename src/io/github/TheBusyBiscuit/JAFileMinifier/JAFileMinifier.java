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
import java.text.SimpleDateFormat;

public class JAFileMinifier {
	
	private static SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	public static void main(String[] args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("No Path specified.");
		}

		StringBuilder path = new StringBuilder();

		for (int i = 0; i < args.length; i++) {
			if (i == 0) path.append(args[i]);
			else path.append(" " + args[i]);
		}

		File directory = new File(path.toString());

		if (!directory.exists() || !directory.isDirectory()) {
			throw new IllegalArgumentException("Specified File must be a directory!");
		}
		
		System.out.println(" Scanning '" + directory.getName() + "'");
		
		int files = 0;

		for (File file: directory.listFiles()) {
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

		System.out.println("");
		System.out.println(" > FINISHED");
		System.out.println(" > Minified " + files + " file(s)!");
		System.out.println("");
	}

	private static boolean handleFileExtension(File file, FileExtension ext) throws IOException {
		System.out.println("  Reading '" + file.getName() + "'");

		String destination = file.getParent() + "/" + file.getName().replace(ext.file, ".min" + ext.file);
		File minified = new File(destination);
		
		if (minified.exists()) {
			System.out.println("    " + file.getName().replace(ext.file, ".min" + ext.file) + " already exists!");

			System.out.println("     Last modified (" + ext.file + "): " + format.format(file.lastModified()));
			System.out.println("     Last modified (.min" + ext.file + "): " + format.format(minified.lastModified()));
			
			if (minified.lastModified() < file.lastModified()) {
				System.out.println("    " + file.getName().replace(ext.file, ".min" + ext.file) + " appears to be outdated!");
			}
			else {
				System.out.println("    " + file.getName().replace(ext.file, ".min" + ext.file) + " appears to be up to date!");
				return false;
			}
		}

		URL url = new URL(ext.url);
		System.out.println("   Type: " + ext.type);

		System.out.println("   Converting...");
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
