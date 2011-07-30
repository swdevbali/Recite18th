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

/*
  HISTORY
  1) July 30, 2011 = First created, some method will be use in Synch and GenerateForm
 */

package recite18th.util;
import java.io.*;
import java.util.*;
public class AutogenerateUtil
{
 /* borrowed from http://www.java-forums.org/new-java/434-how-can-i-get-current-directory.html */
    public static Hashtable readIgnoreList()
    {
        String path = System.getProperty("user.dir") + "//application//";
        System.out.println(path);
        File file = new File(path + "ignore.list");
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;
        Hashtable h = new Hashtable();
        try {
            fis = new FileInputStream(file);

            // Here BufferedInputStream is added for fast reading.
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);

            // dis.available() returns 0 if the file does not have more lines.
            while (dis.available() != 0) {

                // this statement reads the line from the file and print it to
                // the console.
                String ignored = dis.readLine();
                h.put(ignored, ignored);
            }

            // dispose all the resources after using them.
            fis.close();
            bis.close();
            dis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return h;
    }
}