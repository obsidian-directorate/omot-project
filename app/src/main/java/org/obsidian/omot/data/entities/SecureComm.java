package org.obsidian.omot.data.entities;

import android.database.Cursor;

import org.obsidian.omot.data.contracts.SecureCommsContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SecureComm {
    private String messageID;
    private String senderID;
    private String recipientID;
    private String encryptedMessage;
    private long sentAt;
    private Long readAt;
    private Long selfDestructAt;
    private String messageType;
    private String attachmentPath;

    public SecureComm() {}

    public SecureComm(String messageID, String senderID, String recipientID, String encryptedMessage, String messageType) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.recipientID = recipientID;
        this.encryptedMessage = encryptedMessage;
        this.messageType = messageType;
        this.sentAt = System.currentTimeMillis();
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(String encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }

    public Long getReadAt() {
        return readAt;
    }

    public void setReadAt(Long readAt) {
        this.readAt = readAt;
    }

    public Long getSelfDestructAt() {
        return selfDestructAt;
    }

    public void setSelfDestructAt(Long selfDestructAt) {
        this.selfDestructAt = selfDestructAt;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    // Utility methods
    public boolean isRead() {
        return readAt != null;
    }

    public boolean isSelfDestruct() {
        return selfDestructAt != null && System.currentTimeMillis() > selfDestructAt;
    }

    public boolean shouldSelfDestruct() {
        return selfDestructAt != null && System.currentTimeMillis() > selfDestructAt && !isRead();
    }

    public String getFormattedSentAt() {
        return formatTimestamp(sentAt);
    }

    public String getFormattedReadAt() {
        return readAt != null ? formatTimestamp(readAt) : "Unread";
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    public static SecureComm fromCursor(Cursor cursor) {
        SecureComm comm = new SecureComm();
        comm.setMessageID(cursor.getString(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_MESSAGE_ID)));
        comm.setSenderID(cursor.getString(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_SENDER_ID)));
        comm.setRecipientID(cursor.getString(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_RECIPIENT_ID)));
        comm.setEncryptedMessage(cursor.getString(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_ENCRYPTED_MESSAGE)));
        comm.setSentAt(cursor.getLong(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_SENT_AT)));
        comm.setReadAt(cursor.isNull(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_READ_AT))
                ? null : cursor.getLong(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_READ_AT)));
        comm.setSelfDestructAt(cursor.isNull(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_SELF_DESTRUCT_AT))
                ? null : cursor.getLong(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_SELF_DESTRUCT_AT)));
        comm.setMessageType(cursor.getString(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_MESSAGE_TYPE)));
        comm.setAttachmentPath(cursor.getString(cursor.getColumnIndexOrThrow(SecureCommsContract.COLUMN_ATTACHMENT_PATH)));
        return comm;
    }
}