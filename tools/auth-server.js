const express = require("express");
const axios = require("axios");
const app = express();
const port = 3000;

const clientId = "<<PLACEHOLDER>>";
const clientSecret = "<<PLACEHOLDER>>";
const callbackUrl = `http://localhost:${port}/callback/`;

// Logging middleware
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

app.get("/login", (req, res) => {
    const scopes = "user-read-email" +
        " playlist-modify-public" +
        " playlist-modify-private" + 
        " playlist-read-private" + 
        " playlist-read-collaborative" + 
        " user-library-modify" +
        " user-library-read";
    const authorizationUrl = "https://accounts.spotify.com/authorize";
    
    const redirectUrl = authorizationUrl +
        "?response_type=code" +
        "&client_id=" + clientId +
        "&scope=" + encodeURIComponent(scopes) +
        "&redirect_uri=" + encodeURIComponent(callbackUrl);

    res.redirect(redirectUrl);
});

app.get("/callback", (req, res) => {
    const code = req.query.code;

    axios({
        method: "post",
        url: "https://accounts.spotify.com/api/token",
        data: "grant_type=authorization_code" +
            "&code=" + code +
            "&redirect_uri=" + encodeURIComponent(callbackUrl),
        auth: {
            username: clientId,
            password: clientSecret,
        }
    }).then(r => {
        console.log(r.data);
    }).catch(err => {
        console.log(err.response.data);
        console.log(err.response.status);
        console.log(err.response.headers);
    })
});

app.get("/", (req, res) => {
    res.redirect("/login");
})

// Little endpoint to check if server is still alive
app.get("/_/healthz", (req, res) => {
    res.send("OK");
});

app.listen(port);
