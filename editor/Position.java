public class Position {

	private int word;
	private int letter;

	public Position(int word2, int letter2) {
		word = word2;
		letter = letter2;
	}

	public int getWord() {
		return word;
	}

	public void addWord() {
		word++;
	}

	public int getLetter() {
		return letter;
	}

	public void addLetter() {
		letter++;
	}
}