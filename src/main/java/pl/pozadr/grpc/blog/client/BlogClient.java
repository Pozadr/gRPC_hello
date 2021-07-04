package pl.pozadr.grpc.blog.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pl.pozadr.blog.Blog;
import pl.pozadr.blog.BlogServiceGrpc;
import pl.pozadr.blog.CreateBlogRequest;

public class BlogClient {

    ManagedChannel channel;

    public static void main(String[] args) {
        BlogClient blogClient = new BlogClient();
        blogClient.run();
    }

    private void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() // disable SSL
                .build();

        var blogClient = BlogServiceGrpc.newBlockingStub(channel);

        var blog = Blog.newBuilder()
                .setAuthorId("Adrian")
                .setTitle("New Blog")
                .setContent("Blog hello world!!")
                .build();

        var request = CreateBlogRequest.newBuilder()
                .setBlog(blog)
                .build();

        var response = blogClient.createBlog(request);

        System.out.println("Received create blog response");
        System.out.println(response.toString());
    }
}
