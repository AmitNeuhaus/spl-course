package bgu.spl.net.srv.bidi;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.*;


import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<ArrayList<String>> {

    private DataBase dataBase;
    private ConnectionsImpl<ArrayList<String>> connections;
    private int myConnectionId;
    private LinkedList<String> badWords = new LinkedList<>();

    @Override
    public void start(int connectionId, Connections<ArrayList<String>> connections) {
        this.connections = (ConnectionsImpl<ArrayList<String>>) connections;
        this.myConnectionId = connectionId;
        dataBase =  DataBase.getInstance();
        badWords.add("fuck");
        badWords.add("shit");
    }

    @Override
    public void process(ArrayList<String> message) {
        String opCode = message.get(0);
        switch (opCode){
            case "1":
                register(message.get(1),message.get(2),message.get(3), opCode);
                break;
            case "2":
                logIn(message.get(1),message.get(2), message.get(3), opCode);
                break;
            case "3":
                logOut(opCode);
                break;
            case "4":
                boolean followOrUnfollow = message.get(1).equals("0");
                follow(followOrUnfollow, message.get(2),opCode);
                break;
            case "5":
                post(message.get(1),opCode);
                break;
            case "6":
                pm(message.get(1), message.get(2), message.get(3),opCode);
                break;
            case "7":
                logStat(opCode);
                break;
            case "8":
                String usersList = message.get(1);
                stat(usersList, opCode);
                break;
            case "9":
                System.out.println("NOT IMPLEMENTED YET 9");
            case "10":
                System.out.println("NOT IMPLEMENTED YET 10");
            case "11":
                System.out.println("NOT IMPLEMENTED YET 11");
            case "12":
                String userNameToBlock = message.get(1);
                block(userNameToBlock,opCode);
                break;
            default:
                System.out.println("Couldn't recognize op code");
                break;
        }
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }


//    ACTIONS-------
    public void register(String username,String password, String birthday, String opCode){
        if (canRegisterNewUser(username)) {
            dataBase.register(username, password, birthday);
            sendAck(opCode);
        }else {
            sendError(opCode);
        }
    }

    public void logIn(String username,String password,String captcha, String opCode){
        if(canLogIn(username,password,captcha)) {
            dataBase.logIn(myConnectionId, username);
            sendAck(opCode);
            for (ArrayList<String> message : dataBase.getUserInfo(username).getUnreadMessages()){
                connections.send(myConnectionId, message);
            }
        }else{
            sendError(opCode);
        }

    }
    public void logOut(String opCode){
        if(canLogOut(myConnectionId)){
            dataBase.logOut(myConnectionId);
            sendAck(opCode);
            shouldTerminate();
            connections.disconnect(myConnectionId);
        }else {
            sendError(opCode);
        }
    }

    public void follow(boolean action, String userName, String opCode){
        UserInfo user = dataBase.getUserInfo(myConnectionId);
        UserInfo followedUser = dataBase.getUserInfo(userName);

        ArrayList<String> userNameAsList = new ArrayList<>();
        userNameAsList.add(userName);
        if (action) {
            if(canFollow(userName)){
                user.follow(userName);
                followedUser.addFollower(user.getName());
                sendAck(userNameAsList,opCode);
            }else {
                sendError(opCode);
            }
        }else{
            if (canUnfollow(userName)) {
                user.unFollow(userName);
                followedUser.removeFollower(user.getName());
                sendAck(userNameAsList,opCode);
            }else {
                sendError(opCode);
            }
        }
    }

    public void post(String content, String opCode){
        UserInfo user = dataBase.getUserInfo(myConnectionId);
        if(canPost(user)) {
            Post post = new Post(content,badWords);
            user.addPost(post);
            List<String> matchingUsers = post.searchUsersInContent();
            // Add Post to tagged users
            for (String usertag : matchingUsers) {
                String username = usertag.substring(1);
                UserInfo taggedUser = dataBase.getUserInfo(username);
                if(taggedUser != null && !taggedUser.isBlocked(user.getName())) {
                    sendNotificationToUser(taggedUser,post.getContent(),"1");
                }
            }

            //Add Post to followers
            ConcurrentLinkedQueue<String> followers = user.getFollowers();
            for (String follower : followers) {
                UserInfo followerUser = dataBase.getUserInfo(follower);
                sendNotificationToUser(followerUser,post.getContent(),"1");
            }
            sendAck(opCode);
        }else {
            sendError(opCode);
        }

    }


    public void pm(String followerName, String content, String timestamp, String opCode){
        UserInfo user = dataBase.getUserInfo(myConnectionId);
        UserInfo follower = dataBase.getUserInfo(followerName);

        if(canPm(user, follower)) {
            PM pm = new PM(content, timestamp,badWords);
            user.addPm(pm);
            sendNotificationToUser(follower,pm.getContent(),"0");
            sendAck(opCode);
        }else {
            sendError(opCode);
        }
    }


    public void logStat(String opCode){
        UserInfo user = dataBase.getUserInfo(myConnectionId);
        if(user != null && user.isLoggedIn()) {
            ConcurrentHashMap<Integer, String> activeUsers = dataBase.getActiveUsers();
            for(Integer i: activeUsers.keySet()) {
                String userName = activeUsers.getOrDefault(i, null);
                if (userName != null && !user.isBlocked(userName)){
                    UserInfo activeUserInfo = dataBase.getUserInfo(userName);
                    sendAck(activeUserInfo.getStat(), opCode);
                }
            }
        }else {
            sendError(opCode);
        }
    }


    public void stat(String usersList, String opCode){
        String[] interestedUserNames = usersList.split("\\|");
        UserInfo user = dataBase.getUserInfo(myConnectionId);
        if(user != null && user.isLoggedIn()) {

            for(String userName: interestedUserNames) {
                UserInfo activeUserInfo = dataBase.getUserInfo(userName);
                if (activeUserInfo != null) {
                    if (user.isBlocked(userName)){
                        sendError(opCode);
                        return;
                    }
                }
            }

            for(String userName: interestedUserNames) {
                UserInfo activeUserInfo = dataBase.getUserInfo(userName);
                if (activeUserInfo != null){
                     sendAck(activeUserInfo.getStat(),opCode);
                }
            }
        }else {
            sendError(opCode);
        }
    }



    public void block(String username,String opCode){
        UserInfo user = dataBase.getUserInfo(myConnectionId);
        UserInfo blockUser = dataBase.getUserInfo(username);
        if (blockUser != null) {
            user.unFollow(blockUser.getName());
            user.removeFollower(blockUser.getName());
            user.addBlocked(blockUser.getName());
            blockUser.unFollow(user.getName());
            blockUser.removeFollower(user.getName());
            blockUser.addBlocked(user.getName());
            sendAck(opCode);
        }else {
            sendError(opCode);
        }
    }

    // Queries

    private boolean canRegisterNewUser(String userName){
        UserInfo user = dataBase.getUserInfo(userName);
        return (user == null);
    }


    private boolean canLogIn(String username, String password, String captcha) {
        UserInfo user = dataBase.getUserInfo(username);
        boolean userExists = user != null;
        if (userExists){
            boolean matchingPassowrd = user.getPassword().equals(password);
            return (matchingPassowrd && !user.isLoggedIn() && captcha.equals("1"));
        }
        return false;
    }

    private boolean canLogOut(int connectionId) {
        UserInfo user = dataBase.getUserInfo(connectionId);
        boolean userExists = user != null;
        return (userExists && user.isLoggedIn());
    }

    private boolean canFollow(String userName) {
        UserInfo user = dataBase.getUserInfo(myConnectionId);
        UserInfo followed = dataBase.getUserInfo(userName);
        boolean followerExists = followed != null;
        return (followerExists && user.isLoggedIn() && !followed.isFollower(user.getName()) && !followed.isBlocked(user.getName()));
    }

    private boolean canUnfollow(String userName) {
        UserInfo user = dataBase.getUserInfo(myConnectionId);
        UserInfo followed = dataBase.getUserInfo(userName);
        boolean followerExists = followed != null;
        return (followerExists && user.isLoggedIn() && user.isFollowing(userName));
    }

    public boolean canPost(UserInfo user){
        return user.isLoggedIn();
    }

    public boolean canPm(UserInfo user, UserInfo follower){
        return user.isLoggedIn() && follower != null && user.isFollower(follower.getName());
    }

//    HELPERS -------

    private void sendAck(ArrayList<String> optional,String opCode){
        ArrayList<String> ack = new ArrayList<String>();
        ack.add("ACK");
        ack.add(opCode);
        ack.addAll(optional);
        connections.send(myConnectionId, ack);
    }
    private void sendAck(String opCode){
        ArrayList<String> ack = new ArrayList<String>();
        ack.add("ACK");
        ack.add(opCode);
        connections.send(myConnectionId, ack);
    }

    private void sendError(String opCode){
        ArrayList<String> error = new ArrayList<String>();
        error.add("ERROR");
        error.add(opCode);
        connections.send(myConnectionId, error);
    }

    private void sendNotificationToUser(UserInfo receiver, String content,String notificationType){
        ArrayList<String> notification = new ArrayList<>();
        String sender = dataBase.getUsername(myConnectionId);
        notification.add("NOTIFICATION");
        notification.add(notificationType);
        notification.add(sender);
        notification.add(content);
        if (receiver.isLoggedIn()){
            int connectionId = dataBase.getConnectionId(receiver.getName());
            connections.send(connectionId, notification);
        }else{
            receiver.addUnreadMessages(notification);
        }
    }
}