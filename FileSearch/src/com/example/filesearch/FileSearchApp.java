package com.example.filesearch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileSearchApp {
    String path;
    String regex;
    String zipFileName;
    Pattern pattern;
    ArrayList<File> zipFiles = new ArrayList<File>();
	public static void main(String[] args) {
		FileSearchApp app = new FileSearchApp();
		switch(Math.min(args.length, 3)){
		case 0: 
		{
			System.out.println("USAGE: FileSearchApp [path] [regex] [zipFileName]");
			return;
		}
		case 3:app.setZipFileName(args[2]);
		case 2:app.setRegex(args[1]);
		case 1:app.setPath(args[0]);
		}
		try {
		app.walkDirectory(app.getPath());
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
	}
	public void walkDirectory(String path) throws IOException{
		walkDirectoryJava6(path);
		zipFilesJava6();
	}
	
	public void walkDirectoryJava6(String path) throws IOException{
		File Dir = new File(path);
		File[] files= Dir.listFiles();
		for(File file:files) {
			if(file.isDirectory()) {
				walkDirectoryJava6(file.getAbsolutePath());
			} else {
				processFile(file);
			}
		}
		
	}
	
	public void walkDirectoryJava8(String path) throws IOException{
		Files.walk(Paths.get(path))
		.forEach(f -> processFile(f.toFile()));
		
	}
	
	public void processFile(File file) {
		try {
			if(searchFile(file)) {
				addFileToZip(file);
			}
		} catch (IOException|UncheckedIOException e) {
			System.out.println("Error processing file: "+ file + ":" + e);
		}
		
	}
	public boolean searchFile(File file) throws IOException {
		return searchFileJava8(file);
	}
	
	public boolean searchFileJava8(File file) throws IOException{
		return Files.lines(file.toPath(), StandardCharsets.UTF_8)
				.anyMatch(t-> searchText(t));
				
		
	}
	
	private boolean searchText(String text) {
		return (this.getRegex()==null) ? true:
			this.pattern.matcher(text).find();
			
		
	}
	public void addFileToZip(File file) {
		if(getZipFileName() != null) {
			zipFiles.add(file);
		}
	}
	
	public void zipFilesJava7() throws IOException{
		try(ZipOutputStream out = 
				new ZipOutputStream(new FileOutputStream(getZipFileName()))){
			File baseDir = new File(getPath());
			for(File file : zipFiles) {
				String fileName = getRelativeFileName(file,baseDir);
				ZipEntry zipEntry = new ZipEntry(fileName);
				zipEntry.setTime(file.lastModified());
				Files.copy(file.toPath(), out);
				
				out.closeEntry();
			}
		}
	}
	
	public void zipFilesJava6() throws IOException{
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(getZipFileName()));
			File baseDir = new File(getPath());
			
			for(File file: zipFiles) {
				String fileName = getRelativeFileName(file, baseDir);
				
				ZipEntry zipEntry = new ZipEntry(fileName);
				zipEntry.setTime(file.lastModified());
				out.putNextEntry(zipEntry);
				
				int bufferSize = 2048;
				byte[] buffer = new byte[bufferSize];
				int len = 0;
				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream(file), bufferSize);
				while((len=in.read(buffer,0,bufferSize)) != -1) {
					out.write(buffer,0,len);
				}
				in.close();
				
				out.closeEntry();
			}
			
		}finally {
			out.close();
		}
	}
	private String getRelativeFileName(File file, File baseDir) {
		String fileName = file.getAbsolutePath().substring(
				baseDir.getAbsolutePath().length());
		
		fileName = fileName.replace('\\', '/');
		
		while(fileName.startsWith("/")) {
			fileName = fileName.substring(1);
		}
		return fileName;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
	}
	public String getZipFileName() {
		return zipFileName;
	}
	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}
   
}
