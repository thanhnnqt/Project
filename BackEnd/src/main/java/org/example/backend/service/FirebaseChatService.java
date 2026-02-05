package org.example.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.example.backend.entity.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseChatService {

    private static final String COLLECTION_NAME = "chat_messages";

    public void saveMessage(ChatMessage message) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection(COLLECTION_NAME).add(message);
        } catch (Exception e) {
            System.err.println("Failed to save message to Firebase: " + e.getMessage());
        }
    }

    public List<ChatMessage> getChatHistory(String roomId) {
        List<ChatMessage> history = new ArrayList<>();
        try {
            Firestore db = FirestoreClient.getFirestore();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("roomId", roomId)
                    .orderBy("timestamp", Query.Direction.ASCENDING);
            
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
                history.add(document.toObject(ChatMessage.class));
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch chat history from Firebase: " + e.getMessage());
        }
        return history;
    }
}
