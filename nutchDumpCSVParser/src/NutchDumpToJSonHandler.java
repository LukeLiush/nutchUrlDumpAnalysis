import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;

import org.apache.tika.sax.ToTextContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class NutchDumpToJSonHandler extends ToTextContentHandler {
	/**
	 * The character stream.
	 */
	private final Writer writer;
	private StringBuilder chars;
	private LinkedList<String> list;
	private String currrentAttr;

	private static final String TR = "tr";
	private static final String TD = "td";
	private static final String ID = "id";
	private static final String UL = "ul";
	private static final String LI = "li";

	public NutchDumpToJSonHandler(Writer writer) {
		this.writer = writer;
		this.chars = new StringBuilder('"');

		currrentAttr = null;
	}

	public NutchDumpToJSonHandler(OutputStream stream) {
		this(new OutputStreamWriter(stream, Charset.defaultCharset()));
	}

	public NutchDumpToJSonHandler(OutputStream stream, String encoding)
			throws UnsupportedEncodingException {
		this(new OutputStreamWriter(stream, encoding));

	}

	public NutchDumpToJSonHandler() {
		this(new StringWriter());
	}

	@Override
	public void startDocument() throws SAXException {
		write("{\"unfetched_categories\":[");
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		if (localName.equals(TR)) {
			if (currrentAttr != null) {

				write(",{");
			} else {
				write("{");
			}
		} else if (localName.equals(TD)) {
			this.currrentAttr = atts.getValue(ID);
			write("\"");
			write(currrentAttr);
			write("\"");
			write(":");
			chars.setLength(0);

		} else if (localName.equals(UL)) {

			write("[");
			list = new LinkedList<String>();
		}

	}

	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (localName.equals(TR)) {
			write("}");
		} else if (localName.equals(TD) && !currrentAttr.equals("examples")) {
			if (!currrentAttr.equals("isDynamic")) {
				write("\"");
				write(chars.toString());
				write("\",");
			} else {
				write("\"");
				write(chars.toString());
				write("\"");
			}

		} else if (localName.equals(UL)) {
			if (list != null && !list.isEmpty()) {				
				
				String urlstr = list.poll();
				write(urlstr);
				urlstr = list.poll();
				while(urlstr != null){
					write(",");
					write(urlstr);
					urlstr = list.poll();
				}
				write("],");
				list = null;
			}
		}
		try {
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (list != null && length > 4) {
			list.add(new StringBuffer().append(ch).toString());
		} else {
			chars.append(ch, start, length);
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			write("]}");
			writer.flush();
		} catch (IOException e) {
			throw new SAXException("Error flushing character output", e);
		}
	}

	protected void write(final String characters) throws SAXException {
		if (characters != null && characters.length() > 0) {
			write(characters.toCharArray(), 0, characters.length());
		}
	}

	protected void write(char ch[], int start, int length) throws SAXException {
		try {
			if (writer != null) {
				writer.write(ch, start, length);
			}

		} catch (IOException e) {
			throw new SAXException("Error writing: "
					+ new String(ch, start, length), e);
		}
	}

	@Override
	public String toString() {
		return writer.toString();
	}
}
