// index.js in Firebase Functions
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Notify on new upload
exports.notifyOnUpload = functions.firestore
    .document('uploads/{uploadId}')
    .onCreate(async (snap, context) => {
        const uploadData = snap.data();
        const userId = uploadData.userId; // Assuming userId field exists in the upload data
        
        // Create a notification document
        const notificationData = {
            recipientId: userId,
            message: `Your upload was successful.`,
            type: 'upload',
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
        };

        // Add the notification to Firestore
        await admin.firestore().collection('notifications').add(notificationData);
    });

// Notify on new chat message
exports.notifyOnChatMessage = functions.firestore
    .document('chats/{chatId}/messages/{messageId}')
    .onCreate(async (snap, context) => {
        const messageData = snap.data();
        const chatId = context.params.chatId;
        const [user1, user2] = chatId.split('_'); // Assuming chatId format is user1_user2
        const recipientId = messageData.senderId === user1 ? user2 : user1;

        // Create a notification document
        const notificationData = {
            recipientId: recipientId,
            message: `${messageData.senderName} sent you a message.`,
            type: 'chat',
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
        };

        // Add the notification to Firestore
        await admin.firestore().collection('notifications').add(notificationData);
    });
