package org.ieeemadc.devconnect.Utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
//TODO organize the code
public class FireRef {
    public static final FirebaseFirestore Instance=FirebaseFirestore.getInstance();
    public static final FirebaseUser User=FirebaseAuth.getInstance().getCurrentUser();
    public static final CollectionReference Users=Instance.collection("users");
    public static final CollectionReference Posts=Instance.collection("posts");
    public static final CollectionReference Projects=Instance.collection("projects");
    public static final DocumentReference UserDocumet=Instance.collection("users").document(User.getUid());
    public static final CollectionReference UserProjects=UserDocumet.collection("projects");

}