'use-strict'
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
//onWrite triggers if deleted/created or updated 
//it returns a snap with before and after states
//if deleted then after doesnt exist
//firestore add adds and set adds if not created and updates if already exists
exports.onProjectWrite = functions.firestore.document("users/{user_id}/projects/{project_id}").onWrite((change, context) => {
    //a post has an publisher feild
    const project = change.after.exists ? change.after.data() : null; // if deleted then null else get

    //firebase bug

    let { userId, projectId } = context.params;

    if (typeof userId !== "string" || typeof friendId !== "string") {
      console.warn(`Invalid params, expected 'userId' and 'friendId'`, context.params);
      userId = change.after.ref.parent.parent.id;
      friendId = change.after.id;
    }


    //
    if (project === null) 
        return admin.firestore().collection("posts").doc(projectId).delete();
    
    const publisher_promise=admin.firestore().collection("users").doc(userId).get();
    return publisher_promise.then(user =>{
    	const photoURL=user.data().photoURL;
    	const post= new Object();
    post.postID=projectId;
    post.publisherID=userId;
    post.title=project.title;
    post.publisher=project.publisher;
    post.description = project.description;
    post.bannerURL=project.bannerURL;
    post.created = project.created;
    post.votes=project.votes;
    post.publisherPhotoURL=photoURL;
    const update_project= admin.firestore().collection("posts").doc(projectId).set(post);
    const get_all_votes= admin.firestore().collection("users").doc(userId).get();
    return Promise.all([update_project,get_all_votes]).then(results=>{
    	const votes=results[1].data().votes;
    	var user=results[1].data();
    	user.votes = votes+1;
    	console.log("notification_id:  ", user);console.log("notification_id:  ", user.data());
    	console.log("notification_id:  ", user.votes);
    	console.log("notification_id:  ", votes);
    	return admin.firestore().collection("users").doc(userId).set(user);
    });
    
    });
});


//notification: sender ,body
//user: name,photoURL,token
exports.sendNotificaion = functions.firestore.document("users/{user_id}/notifications/{notification_id}").onCreate((snap, context)=> { 
     const notification = snap.data();
     const sender=notification.sender;

      //const from_data=admin.firestore().collection("users").doc(from).get();
      //const to_data=admin.firestore().collection("users").doc(to).get();

      notification.id=context.params.notification_id;
       console.log("notification_id:  ", notification.id);
      const save_id=admin.firestore().collection("users").doc(context.params.user_id).collection("notifications").doc(context.params.notification_id).set(notification);
      const sender_promise=admin.firestore().collection("users").doc(sender).get();
      const reciever_promise=admin.firestore().collection("users").doc(context.params.user_id).get();
            	//console.log("user_id", context.params.user_id);
      return Promise.all([sender_promise,reciever_promise,save_id]).then(results =>{
      	const token=results[1].data().token;
      	const user=results[0];
      	var body;
      	if(typeof notification.type !== 'undefined')
      		{switch(notification.type) {
				  case "joinRequest":
				    body=user.data().name + " requests to join your project:\n"+notification.projectName;
				    break;
				}
			}
		else body=notification.body;
      	const payload={
      			notification: {
      				title: user.data().name,
      				body: body,
      				icon: user.data().photoURL
      			}	
      		};
      	//console.log("name", payload.notification.title);
      	//console.log("body", payload.notification.body);
      	//console.log("url", payload.notification.icon);
      	//console.log("token", token);
      	admin.messaging().sendToDevice(token,payload)
      	.then(function (response) {
                    console.log("Successfully sent message:", response);
                    return null;
                })
                .catch(function (error) {
                    console.log("Error sending message:", error);
                });


      	return null;
      });

});

//

