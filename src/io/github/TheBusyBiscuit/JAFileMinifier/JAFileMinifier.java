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

	public static void main(String[] args) throws IOException {
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

		System.out.println("Scanning '" + directory.getName() + "'");

		for (File file: directory.listFiles()) {
			if (file.getName().endsWith(".js") && !file.getName().endsWith(".min.js")) {
				System.out.println(" Reading '" + file.getName() + "'");

				URL url = new URL("https://javascript-minifier.com/raw");
				System.out.println("  Type: JS (JavaScript)");

				System.out.println("  Converting...");
				byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
				bytes = ("input=" + URLEncoder.encode(new String(bytes), "UTF-8")).getBytes("UTF-8");

				System.out.println("  Connecting... '" + url.toString() + "'");
				final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("User-Agent", "Minify (by TheBusyBiscuit)");
				connection.setRequestProperty("charset", "utf-8");
				connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
				connection.setDoOutput(true);
				connection.setConnectTimeout(7000);

				System.out.println("  Sending data... (" + bytes.length + " Bytes)");
				DataOutputStream output = new DataOutputStream(connection.getOutputStream());
				output.write(bytes);

				final int code = connection.getResponseCode();

				System.out.println("  Response: " + code);

				String destination = file.getParent() + "/" + file.getName().replace(".js", ".min.js");

				if (code == 200) {
					System.out.println("  Exporting '" + destination + "'");
				    ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
					FileOutputStream fos = new FileOutputStream(destination);
				    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				    fos.close();
				    connection.disconnect();
					System.out.println("  Done!");
				}
				else {
				    System.err.println("Could not connect to Webservice.");
				}
			}
			else if (file.getName().endsWith(".css") && !file.getName().endsWith(".min.css")) {
				System.out.println(" Reading '" + file.getName() + "'");

				URL url = new URL("https://cssminifier.com/raw");
				System.out.println("  Type: CSS (Cascading Style Sheet)");

				System.out.println("  Converting...");
				byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
				bytes = ("input=" + URLEncoder.encode(new String(bytes), "UTF-8")).getBytes("UTF-8");

				System.out.println("  Connecting... '" + url.toString() + "'");
				final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("User-Agent", "Minify (by TheBusyBiscuit)");
				connection.setRequestProperty("charset", "utf-8");
				connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
				connection.setDoOutput(true);
				connection.setConnectTimeout(7000);

				System.out.println("  Sending data... (" + bytes.length + " Bytes)");
				DataOutputStream output = new DataOutputStream(connection.getOutputStream());
				output.write(bytes);

				final int code = connection.getResponseCode();

				System.out.println("  Response: " + code);

				String destination = file.getParent() + "/" + file.getName().replace(".css", ".min.css");

				if (code == 200) {
					System.out.println("  Exporting '" + destination + "'");
				    ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
					FileOutputStream fos = new FileOutputStream(destination);
				    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				    fos.close();
				    connection.disconnect();
					System.out.println("  Done!");
				}
				else {
				    System.err.println("Could not connect to Webservice.");
				}
			}
		}
	}

}
