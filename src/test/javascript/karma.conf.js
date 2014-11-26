// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

module.exports = function (config) {
    config.set({
        // base path, that will be used to resolve files and exclude
        basePath: '../../..',

        // testing framework to use (jasmine/mocha/qunit/...)
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: [
            'src/main/webapp/bower_components/patternfly/components/html5shiv/dist/html5shiv.min.js',
			'src/main/webapp/bower_components/patternfly/components/respond/dest/respond.min.js',
            'src/main/webapp/bower_components/patternfly/components/jquery/jquery.js',
            'src/main/webapp/bower_components/patternfly/components/bootstrap/dist/js/bootstrap.min.js',
            'src/main/webapp/bower_components/patternfly/components/datatables/media/js/jquery.dataTables.js',
            'src/main/webapp/bower_components/patternfly/dist/js/patternfly.min.js',
            'src/main/webapp/scripts/*.js',
            'src/main/webapp/scripts/**/*.js',
            'src/test/javascript/**/!(karma.conf).js'
        ],

        // list of files / patterns to exclude
        exclude: [],

        // web server port
        port: 9876,

        // level of logging
        // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
        logLevel: config.LOG_INFO,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: false,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS'],

        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: false
    });
};
