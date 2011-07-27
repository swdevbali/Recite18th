package recite18th.library;

/**
 *
 * @author samsonasik
 */
//dimodifikasi dari kelas paginationnya codeIgniter
public class Pagination {

    public static int totalrows = 5;
    public static int numlink = 2;
    public static int curpage = 0;
    public static int perpage = 4;
    public static String firstlink = "First";
    public static String nextlink = "Next";
    public static String prevlink = "Previous";
    public static String lastlink = "Last";

    public static String createLinks(int halaman) {
        if ((totalrows == 0) || (perpage == 0)) {
            return "";
        }

        int numpage = totalrows / perpage;
        int sisa = totalrows % perpage;
        if (sisa > 0) {
            numpage = numpage + 1;
        }

        if (numpage == 1) {
            return "";
        }

        try {
            curpage = halaman;
        } catch (Exception e) {
            curpage = 1;
        }

        try {
            curpage = (int) curpage;
        } catch (Exception e) {
            curpage = 1;
        }

        if (curpage > totalrows) {
            curpage = (numpage - 1) * perpage;
        }

        if (curpage < 1) {
            curpage = 1;
        }

        int uri_page_number = curpage;
        int start = ((curpage - numlink) > 0) ? curpage - (numlink - 1) : 1;
        int end = ((curpage + numlink < numpage)) ? curpage + numlink : numpage;

        //inisialisasi ...
        String theoutput = "";
        //Render first link
        if (curpage > (numlink + 1)) {
            if (curpage <= numpage) {
                theoutput += "<a href=\"?pagenum=1\">" + firstlink + "</a> ";
            } else {
                theoutput += "";
            }
        }

        //Render Previous Link
        if (curpage != 1) {
            if (curpage <= numpage) {
                theoutput += "<a href=\"?pagenum=" + (curpage - 1) + "\">" + prevlink + "</a> ";
            } else {
                theoutput += "";
            }
        }

        //render digit link

        for (int loop = start - 1; loop < end; loop++) {
            if (curpage <= numpage) {
                if (curpage == (loop + 1)) {
                    theoutput += loop + 1 + " ";
                } else {
                    theoutput += "<a href=\"?pagenum=" + (loop + 1) + "\">" + (loop + 1) + "</a> ";
                }
            } else {
                theoutput += "";

            }
        }

        //render the next link
        if (curpage < numpage) {
            theoutput += "<a href=\"?pagenum=" + (curpage + 1) + "\">" + nextlink + "</a> ";
        } else {
            theoutput += "";
        }

        //render the last link
        if ((curpage + numlink) < numpage) {
            theoutput += "<a href=\"?pagenum=" + numpage + "\">" + lastlink + "</a>";
        }

        return theoutput;
    }
}
