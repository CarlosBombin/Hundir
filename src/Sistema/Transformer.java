package Sistema;

public class Transformer {
	private static String letras[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	
	public static char numToLetter(int i) {
	    if (i >= 0 && i <= 25) {
	        return letras[i].charAt(0);
	    } else {
	        return '?';
	    }
	}
	
	public static int letterToNum(char a) {
	    for (int i = 0;i <= 25;i++) {
	    	if (String.valueOf(a).equals(letras[i])) {
	        return i;
	    	}
	    }
	    return 999;
	}
}
