package io.github.TheBusyBiscuit.JAFileMinifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JAFileMinifier {
	
	private static List<String> blacklist = new ArrayList<String>();
	
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length < 1) {
			throw new IllegalArgumentException("Usage: <manual/auto>");
		}
		
		if (!CompilerMode.isValid(args[0].toUpperCase())) {
			throw new IllegalArgumentException("Usage: <manual/auto>");
		}
		
		File blacklistFile = new File(System.getProperty("user.dir") + "/JAFileMinifier.blacklist");
		
		if (blacklistFile.exists()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(blacklistFile), StandardCharsets.UTF_8));
			
			String line;
		    while ((line = reader.readLine()) != null) {
		    	if (!line.matches("[ \t\n\r]+")) blacklist.add(line);
		    }
		    
		    reader.close();
		}
		
		CompilerMode mode = CompilerMode.valueOf(args[0].toUpperCase());

		switch (mode) {
		case AUTO: {
			System.out.println(">>> JAFileMinifier is watching for changes. Press CTRL+C to stop.");
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
		File file = new File(System.getProperty("user.dir"));
		
		int files = run(file);
		
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
				String path = file.getPath().replace(System.getProperty("user.dir") + "\\", "");
				if (!isBlacklisted(path)) {
					files += run(file);
				}
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
		String path = file.getPath().replace(System.getProperty("user.dir") + "\\", "");
		
		if (isBlacklisted(path)) return false;
		
		File minified = new File(destination);
		
		if (minified.exists()) {
			if (minified.lastModified() < file.lastModified()) {
				System.out.println("  " + path.replace(ext.file, ".min" + ext.file) + " appears to be outdated!");
			}
			else {
				return false;
			}
		}
		else {
			System.out.println("  " + path);
		}
		
		System.out.println("   Type: " + ext.type);

		switch (ext) {
			case JSON: {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
				String content = "";
				
				String line;
			    while ((line = reader.readLine()) != null) {
			    	content += line;
			    }
			    
			    reader.close();
			    
			    StringBuilder builder = new StringBuilder();
			    String inner = null;
			    
			    for (int i = 0; i < content.length(); i++){
			        char c = content.charAt(i);        
			        
			        if (c == '"') {
			        	if (inner == null) {
			        		inner = "\"";
			        	}
			        	else if (inner.equals("\"")) {
			        		inner = null;
			        	}
			        }
			        else if (c == '\'') {
			        	if (inner == null) {
			        		inner = "'";
			        	}
			        	else if (inner.equals("'")) {
			        		inner = null;
			        	}
			        }
			        
			        if (c == ' ') {
			        	if (inner != null) 
			        		builder.append(c);
			        }
			        else if (c == '\t') {
			        	if (inner != null) 
			        		builder.append(c);
			        }
			        else {
			        	builder.append(c);
			        }
			    }

				System.out.println("   Exporting '" + destination + "'");
				
			    BufferedWriter writer = new BufferedWriter(new FileWriter(destination));
			    writer.write(builder.toString());
			    writer.close();
				System.out.println("  Done!");
				return true;
			}
			default: {
				URL url = new URL(ext.url);
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
					System.out.println("   Exporting '" + path + "'");
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
	}

	public static boolean isBlacklisted(String path) {
		for (String regex: blacklist) {
			if (path.matches(regex)) return true;
		}
		return false;
	}

}
