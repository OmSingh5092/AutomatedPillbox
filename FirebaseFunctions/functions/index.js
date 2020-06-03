const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();


exports.reminderTrigger = functions.database.ref('boxes/{boxid}/reminders/{rem}')
    .onCreate((snapshot,context)=>{

        var data = snapshot.val();
        console.log("Data:",data);

        console.log("BoxId", context.params.boxid);


        //Generating Title

        var message = {
            data:{
                type: "reminder",
                boxname:context.params.boxid,
                name:data.name,
                time:data.time
            },
            topic : context.params.boxid
        };

        console.log("Payload:",message);

        admin.messaging().send(message).then((res)=>{
            console.log("Message sent successfully",res);
            return null;
        }).catch((err)=>{
            console.log("Error:", err);
        })


    })


exports.initiateMetaData = functions.firestore.document('users/{doc}')
    .onCreate((snap,context)=>{
        var data = snap.data().userprofile;
        var metadata = {
            name: data.firstname+" "+data.lastname,
            email:data.email
        }
        return admin.firestore().collection("usersMetadata").doc(context.params.doc).set(metadata).then((res)=>{
            console.log("Users Metadata:" ,metadata);
            return null
        }).catch((err)=>{
            console.log(err);
        })
    })


exports.subscribeUsers = functions.database.ref('boxes/{boxid}/uid/{newuid}')
    .onCreate((snapshot,context)=>{
        var clienttokens;
        return admin.firestore().collection("registrationToken").doc(context.params.newuid).get()
            .then((doc)=>{
                var tokens = doc.data().tokens;
                console.log("Tokens",tokens);
                clienttokens = tokens;
                return admin.messaging().subscribeToTopic(tokens,context.params.boxid);
                
            }).then((response)=>{
                console.log("Subscibed to the topics!");

                var message = {
                    data:{
                        type:"newbox",
                        boxid:context.params.boxid
                    },
                    token: context.params.boxid
                }
                return admin.messaging().send(message)

            }).then((res)=>{
                console.log("Message Sent Successfully:",res);

                return admin.firestore().collection("users").doc(context.params.newuid).update({
                    newboxes: admin.firestore.FieldValue.arrayUnion(context.params.boxid)
                });
            }).then(()=>{
                console.log("NewBox Request updated");
                return null;

            }).catch((err)=>{
                console.log(err);
            })
    })

exports.unsubscribeUsers = functions.database.ref('boxes/{boxid}/uid/{newuid}')
    .onDelete((snapshot,context)=>{
        var boxnamesField = "boxnames"+context.params.boxid;
        return admin.firestore().collection("users").doc(context.params.newuid).update({
            "boxes": admin.firestore.FieldValue.arrayRemove(context.params.boxid),
            boxnamesField: admin.firestore.FieldValue.delete(),
            "newboxes": admin.firestore.FieldValue.arrayRemove(context.params.boxid)

        }).then((response)=>{
            console.log("Box deleted!");
            var message = {
                data:{
                    type:"deletebox",
                    boxid:context.params.boxid
                },
                topic: context.params.boxid
            }
            return admin.messaging().send(message);   

        }).then((res)=>{
            console.log("Message Sent Successfully:",res);

            return admin.firestore().collection("registrationToken").doc(context.params.newuid).get()
        }).then((doc)=>{
            var tokens = doc.data().tokens;
            console.log("Tokens",tokens);
            
            
            return admin.messaging().unsubscribeFromTopic(tokens,context.params.boxid);
            
        }).then(()=>{
            console.log("Unsubscribed Successfully");
            return null;         
        }).catch((err)=>{
            console.log(err);
        })
    })


