import HttpRequests.HttpRequester;

/**
 * Created by Ilya on 7/11/16.
 */
public class tester {
    public static void main(String[] args) {
//        HttpRequester.requester("http://localhost:3000/posts","  data: {\n" +
//                "    title: 'foo',\n" +
//                "    body: 'bar',\n" +
//                "    userId: 1\n" +
//                "  }","POST");

        System.out.println(HttpRequester.requester("http://jsonplaceholder.typicode.com/posts/","","GET").get("id"));
    }
}
