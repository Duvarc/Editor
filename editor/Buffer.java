package editor;
import java.util.LinkedList;

public class Buffer {
	private LinkedList<Character> text;
	private int currentPos;

	public Buffer() {
		text = new LinkedList<Character>();
		currentPos = 0;
	}

	public int currentPos() {
		return currentPos;
	}

	public void addChar(char x) {
		text.add(x);
		currentPos++;
	}

	public void deleteChar() {
		text.remove(currentPos - 1);
		currentPos--;
	}

	public char get(int i) {
		return text.get(i);
	}

	public String toString() {
		String s = "";
		for (int i = 0; i< text.size(); i++) {
			s  = s + text.get(i);
		}
		return s;
	}


}