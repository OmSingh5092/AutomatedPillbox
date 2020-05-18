const functions = require('firebase-functions');

const admin = require('firebase-admin');

admin.initializeApp();



exports.notifyReminder = functions.https.onRequest((req,res)=> {
    var date = new Date(req.body.time);
    var timeString = date.getDay()+"/"+date.getMonth()+" " + date.getHours()+":"+date.getMinutes();
    var message = {
        notification:{
            title : "Missed " + req.body.name,
            body : "You have Missed your medication at " + timeString
        }
    };

    return admin.messaging().sendToTopic('reminder',message).then(function(res){
        console.log("Notification Sent successfully",res);
        return null;
    }).catch(function(error){
        console.log(error)
    })

})