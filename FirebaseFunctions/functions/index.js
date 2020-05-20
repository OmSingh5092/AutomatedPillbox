const functions = require('firebase-functions');
const express = require('express');
const bodyParser = require('body-parser');
const reminder = require('./routes/Reminder.js');
const admin = require('firebase-admin');
const cors = require('cors');
const app = express();
app.use(cors({ origin: true }));
app.use('/reminder',reminder);

admin.initializeApp();
exports.notifyReminder = functions.https.onRequest(app);