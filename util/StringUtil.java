/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package recite18th.util;

/**
 *
 * @author Eko SW
 */
public class StringUtil {
    public static String firstCap(String inputWord){
        String firstLetter = inputWord.substring(0,1);  // Get first letter
        String remainder   = inputWord.substring(1);    // Get remainder of word.
        return firstLetter.toUpperCase() + remainder;

    }
}
