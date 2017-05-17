var exec = require('cordova/exec');

exports.send = function(arg0, success, error) {
    exec(success, error, "EmailClient", "coolMethod", [arg0]);
};
