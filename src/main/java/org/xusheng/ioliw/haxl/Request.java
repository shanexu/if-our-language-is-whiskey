package org.xusheng.ioliw.haxl;

public class Request {

    public static class User {
        private final Long id;
        private final String username;


        public User(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
        }
    }

    private final Long id;

    public Long getId() {
        return id;
    }

    public Request(Long id) {
        this.id = id;
    }

}
