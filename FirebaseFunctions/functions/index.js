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
                boxname:context.params.boxid,
                name:data.name,
                time:data.time
            },
            topic : context.params.boxid
        }

        console.log("Payload:",message);

        admin.messaging().send(message).then((res)=>{
            console.log("Message sent successfully",res);
            return null;
        }).catch((err)=>{
            console.log("Error:", err);
        })

        return null;


    })
