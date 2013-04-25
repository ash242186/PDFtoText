package pdf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;



/**
 * @author ashwini.chandel
 * Outlines_extractor :- a java program that extract the outline from pdf and and split txt from two bookmark chapter and write a txt file 
 */
public class Outlines_extractor {

	private static ArrayList<PDOutlineItem> oulines = new ArrayList<PDOutlineItem>();
	private static Map<File, ArrayList<PDOutlineItem>> temp_folders = new Hashtable<File, ArrayList<PDOutlineItem>>();
	private static Map<File, ArrayList<String>> temp_files = new Hashtable<File, ArrayList<String>>();
	
	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		PDDocument doc = null;
		OutputStream os = null;
	    Writer writer = null;
	    try{
	    	// pdf file that want to extract
		File g = new File("cano.pdf");
		doc = PDDocument.load(g);
		PDDocumentOutline root = doc.getDocumentCatalog().getDocumentOutline();
		PDOutlineItem Rootitem = root.getFirstChild();
		//folder name where you want to extract the chapters
		File books = new File("Books");
		books.mkdirs();
		temp_files.put(books, new ArrayList<String>());
		//if pdf have multiple books
		while (Rootitem != null) {
			System.out.println("Book:" + Rootitem.getTitle());
			File book = new File( books, Rootitem.getTitle());
			temp_files.get(books).add(Rootitem.getTitle());
			if(book.mkdirs()){
				temp_files.put(book, new ArrayList<String>());
				temp_folders.put(book, new ArrayList<PDOutlineItem>());
				child_outline(Rootitem.getFirstChild(), book);
			}
			Rootitem = Rootitem.getNextSibling();
		}
		
		System.out.println("\n\n\n"+oulines.size());
		System.out.println("\n\n\n"+temp_folders.size());
		
		
		//now create text file of chapter and chapter mapping
		Iterator<Entry<File, ArrayList<PDOutlineItem>>>   it = temp_folders.entrySet().iterator();
		int zz=0;
		while (it.hasNext()) {
			Map.Entry<File, ArrayList<PDOutlineItem>> pairs = (Map.Entry<File, ArrayList<PDOutlineItem>>)it.next();
			System.out.println(pairs.getKey()+ " = " + pairs.getValue());
			temp_files.put(pairs.getKey(), new ArrayList<String>());
			for(int i=0;i<pairs.getValue().size();i++){
				PDFTextStripper stripper = new PDFText2HTML("UTF-8");
				stripper.setForceParsing(true);
				stripper.setSortByPosition( false );
				int j = oulines.indexOf(pairs.getValue().get(i));
				stripper.setStartBookmark(oulines.get(j));
				if(oulines.size()==j+1){
					stripper.setEndPage(doc.getNumberOfPages());
				}else
					stripper.setEndBookmark(oulines.get(j+1));
				//File outFile = new File(pairs.getKey(), oulines.get(j).getTitle().replace(" ", "_#_").replace(".", "$$").toLowerCase()+".txt");
				temp_files.get(pairs.getKey()).add(zz+".txt~~~"+oulines.get(j).getTitle());
				File outFile = new File(pairs.getKey(), zz+".txt");
		        os = new FileOutputStream(outFile);
		        writer = new OutputStreamWriter(os);
		 
		        stripper.writeText(doc, writer);
		        zz++;
			}
		}
		writeindex();
	    }catch (IOException e) {e.printStackTrace();}
	    finally{
	    	
	    	
	    	if(writer!=null)
	    		try {
	    			writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	
	    	if(os!=null)
				try {
					os.flush();
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(doc!=null)
					try {
						doc.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	    }
	}
	//book extractor
	private static void child_outline(PDOutlineItem item, File parent) {
		
		while (item != null) {
			System.out.println("    Child:" + item.getTitle());
			//temp_files.get(parent).add(item.getTitle());
			if(item.getOpenCount()!=0){
				File chapters = new File(parent, item.getTitle());
				temp_files.get(parent).add(item.getTitle());
				//System.out.println(item.getTitle()+"++++++++++++");
				chapters.mkdirs();
				temp_files.put(chapters, new ArrayList<String>());
				temp_folders.put(chapters, new ArrayList<PDOutlineItem>());
				sub_child_outline(item.getFirstChild(), chapters);
			}else{
				temp_files.get(parent).add(item.getTitle());
				temp_folders.get(parent).add(item);
				oulines.add(item);
			}
			item = item.getNextSibling();
		}
	}
	//chapter extractor
	private static void sub_child_outline(PDOutlineItem item, File chapters) {
		
		while (item != null) {
			System.out.println("      Sub-Child:" + item.getTitle());
			temp_files.get(chapters).add(item.getTitle());
			temp_folders.get(chapters).add(item);
			oulines.add(item);
			item = item.getNextSibling();
		}
		
		
	}
	//write index file in there book/part
	private static void writeindex(){
		Iterator<Entry<File,ArrayList<String>>> it = temp_files.entrySet().iterator();
        while (it.hasNext()) {
			Map.Entry<File,ArrayList<String>> pairs = (Map.Entry<File,ArrayList<String>>)it.next();
			File file = new File(pairs.getKey(), "index.txt");
			WriteTextFileExample(file, pairs.getValue());
        }
	}
	//write text file from string arraylist 
	private static void WriteTextFileExample(File f , ArrayList<String> txt) {
	        Writer writer = null;

	        try {
	            writer = new BufferedWriter(new FileWriter(f));
	            for(String tx:txt){
	            	writer.write(tx+"\n");
	            }
	            
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                if (writer != null) {
	                    writer.close();
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    
	}
}
