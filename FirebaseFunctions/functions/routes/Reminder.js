const express = require('express');
const admin = require('firebase-admin');
const bodyParser = require('body-parser');
const cors = require('cors');
const reminder = express.Router();
reminder.use(cors({ origin: true }));
reminder.route('/').all((req,res,next) =>{
    var date = new Date(parseInt(req.body.time));
    var timeString = date.getDay()+"/"+date.getMonth()+" " + date.getHours()+":"+date.getMinutes();
    var message = {
        notification:{
            title : "Missed " + req.body.name,
            body : "You have Missed your medication at " + timeString
        }
    };

    return admin.messaging().sendToTopic('reminder',message).then(function(){
        console.log("Notification Sent successfully" + req.body.name + " " + req.body.time);
        res.send("Successfull" + res.body.time + " " + res.body.name)
        return null;
    }, (error)=>{
        console.log('Error:',error);
        return null;
    });
})

module.exports = reminder;