package pl.pozadr.grpc.blog.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pl.pozadr.blog.*;

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

        //createBlog(channel);
        readBlog(channel,"60e1e01fef63c75c58f3bbf1");
        updateBlog(channel, "60e1e01fef63c75c58f3bbf1");
        readBlog(channel,"60e1e01fef63c75c58f3bbf1");


        channel.shutdown();
    }

    private void createBlog(ManagedChannel channel) {
        System.out.println("Creating Blog");
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

    private void readBlog(ManagedChannel channel, String blogId) {
        System.out.println("Reading Blog");
        var blogClient = BlogServiceGrpc.newBlockingStub(channel);

        var request = ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build();

        var response = blogClient.readBlog(request);

        System.out.println("Received read blog response");
        System.out.println(response.toString());
    }

    private void updateBlog(ManagedChannel channel, String blogId) {
        System.out.println("Updating Blog");
        var blogClient = BlogServiceGrpc.newBlockingStub(channel);

        var newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Admin")
                .setTitle("Update Blog")
                .setContent("Blog Updated !")
                .build();

        var request = UpdateBlogRequest.newBuilder()
                .setBlog(newBlog)
                .build();

        var response = blogClient.updateBlog(request);
        System.out.println("Blog updated. Response:");
        System.out.println(response.toString());
    }

}
