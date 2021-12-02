package de.bnder.taskmanager.utils.permissions;

public enum GroupPermission {

    //Needed to execute "group create" command
    CREATE_GROUP,
    //Needed to execute "group delete" command
    DELETE_GROUP,
    //Needed to execute "group add" command
    ADD_MEMBERS,
    //Needed to execute "group remove" command
    REMOVE_MEMBERS,
    //Needed to execute "group members" command
    SHOW_MEMBERS,
    //Needed to execute "group notification" command
    DEFINE_NOTIFY_CHANNEL

}
