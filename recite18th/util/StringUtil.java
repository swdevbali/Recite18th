/*
    Recite18th is a simple, easy to use and straightforward Java Database 
    Web Application Framework. See http://code.google.com/p/recite18th
    Copyright (C) 2011  Eko Suprapto Wibowo (swdev.bali@gmail.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package recite18th.util;

import java.util.*;

public class StringUtil {
    public static String firstCap(String inputWord){
        String firstLetter = inputWord.substring(0,1);  // Get first letter
        String remainder   = inputWord.substring(1);    // Get remainder of word.
        return firstLetter.toUpperCase() + remainder;
    }

    public static String toProperClassName(String tableName)
    {
        String className="";
        StringTokenizer st = new StringTokenizer(tableName, "_");
        while(st.hasMoreTokens())
        {
            className = className+StringUtil.firstCap(st.nextToken());
        }
        return className;
    }

    public static String toProperFieldTitle(String fieldName)
    {
        String className="";
        StringTokenizer st = new StringTokenizer(fieldName, "_");
        while(st.hasMoreTokens())
        {
            className = className+StringUtil.firstCap(st.nextToken());
            if(st.hasMoreTokens()) className = className  + " ";
        }
        return className;
    }
}
