angular.module('MsalAngular', [])
    .provider('msalAuthenticationService', function () {
        var _msal = null;
        var _config = null;
        var _constants = null;
        var _oauthData = { isAuthenticated: false, userName: '', loginError: '', idToken: {} };

        var updateDataFromCache = function (scopes) {

            var account =  _msal.browserStorage.getItem("msalAccount" , "Account");
            var idTokenClaims =  _msal.browserStorage.getItem("msalIdToken" , "Credential");
            var idToken =  _msal.browserStorage.getItem("msalIdTokenValue" , "Credential");

            _oauthData.isAuthenticated = idTokenClaims != null && account !== null && idToken.length > 0;
            if(account){
                _oauthData.userName = account.username;
                _oauthData.idToken = idTokenClaims;
            }
        };

        this.init = function (configOptions, httpProvider) {
            if (configOptions) {
                _config = configOptions;
                if (!configOptions.optionalParams) {
                    configOptions.optionalParams = {};
                }

                configOptions.optionalParams.isAngular = true;

                if (httpProvider && httpProvider.interceptors) {
                    httpProvider.interceptors.push('ProtectedResourceInterceptor');
                }

                // create instance with given config
                _msal = new msal.PublicClientApplication(configOptions);

                if (configOptions.routeProtectionConfig) {
                    _msal.routeProtectionConfig = configOptions.routeProtectionConfig;
                }
                else {
                    _msal.routeProtectionConfig = {
                        consentScopes: ["User.Read"]
                    };
                }
                _msal.loginScopes = [_msal.clientId];

            } else {
                throw new Error('You must set configOptions, when calling init');
            }

            updateDataFromCache(_msal.loginScopes);
        };

        // special function that exposes methods in Angular controller
        // $rootScope, $window, $q, $location, $timeout are injected by Angular
        this.$get = ['$rootScope', '$window', '$q', '$location', '$timeout', '$injector', function ($rootScope, $window, $q, $location, $timeout, $injector) {

            var locationChangeHandler = function (event, newUrl, oldUrl) {
                if ($location.$$html5) {
                    var hash = $location.hash();
                }
                else {
                    var hash = '#' + $location.path();
                }
                _msal.handleRedirectPromise().then(handleResponse).catch(err => {
                    console.error(err);
                });
                $timeout(function () {
                    updateDataFromCache(_msal.loginScopes);
                    $rootScope.userInfo = _oauthData;
                }, 1);
            };

            var  handleResponse = function(resp) {

                if (resp !== null) {
                    _oauthData.idToken = resp.idTokenClaims;

                    _msal.browserStorage.setItem("msalAccount" , resp.account , "Account");
                    _msal.browserStorage.setItem("msalIdToken" , resp.idTokenClaims , "Credential");
                    _msal.browserStorage.setItem("msalIdTokenValue" , resp.idToken , "Credential");

                    _oauthData.isAuthenticated = true;
                    _oauthData.userName = resp.account.username;
                    //Refresh the page after obtaining the login information
                    $window.location.reload();
                }
                else {

                    const currentAccounts = _msal.getAllAccounts();
                    if (currentAccounts === null) {
                        return;
                    } else if (currentAccounts.length > 1) {
                        // Add choose account code here
                        console.warn("Multiple accounts detected.");
                    } else if (currentAccounts.length === 1) {

                        var account =  _msal.browserStorage.getItem("msalAccount" , "Account");
                        var idToken =  _msal.browserStorage.getItem("msalIdToken" , "Credential");
                        _oauthData.isAuthenticated = true;
                        _oauthData.userName = account.username;
                        _oauthData.idToken = idToken;
                    }
                }
                updateDataFromCache();
                $rootScope.userInfo = _oauthData;
            };

            var loginHandler = function (loginStartPage, routeProtectionConfig) {
                if (loginStartPage !== null) {
                    _msal._cacheStorage.setItem(_constants.angularLoginRequest, loginStartPage);
                }
                $rootScope.$broadcast('msal:loginRedirect');
                if (routeProtectionConfig.popUp) {
                    _msal.loginPopup(routeProtectionConfig.consentScopes);
                }
                else {
                    _msal.loginRedirect(routeProtectionConfig.consentScopes);
                }
            };

            var routeChangeHandler = function (e, nextRoute) {
                if (nextRoute && nextRoute.$$route) {
                    var requireLogin = _msal.routeProtectionConfig.requireLogin || nextRoute.$$route.requireLogin;
                    if (requireLogin) {
                        if (!_oauthData.isAuthenticated) {
                            if (!_msal.interactionInProgress()) {
                                loginHandler(null, _msal.routeProtectionConfig);
                            }
                        }
                    }
                    else {
                        var nextRouteUrl;
                        if (typeof nextRoute.$$route.templateUrl === "function") {
                            nextRouteUrl = nextRoute.$$route.templateUrl(nextRoute.params);
                        } else {
                            nextRouteUrl = nextRoute.$$route.templateUrl;
                        }
                    }
                }
            };

            // Route change event tracking to receive fragment and also auto renew tokens
            $rootScope.$on('$routeChangeStart', routeChangeHandler);

            $rootScope.$on('$locationChangeStart', locationChangeHandler);

            updateDataFromCache(_msal.loginScopes);
            $rootScope.userInfo = _oauthData;

            return {
                // public methods will be here that are accessible from Controller
                loginRedirect: function (scopes) {
                    _msal.loginRedirect(scopes);
                },

                _msal : _msal,

                loginInProgress: function () {
                    return _msal.interactionInProgress();
                },

                logout: function () {
                    _msal.logout(_msal.getAllAccounts()[0].userName);
                },

                isAuthenticated : function() {
                    return _oauthData.isAuthenticated;
                },

                updateState : function(){
                    $rootScope.userInfo = _oauthData;
                },
                userInfo: _oauthData,
            };
        }];
    });

// Interceptor for http if needed
angular.module('MsalAngular')
    .factory('ProtectedResourceInterceptor', ['msalAuthenticationService', '$q', '$rootScope', '$templateCache', '$injector' , '$window', function (authService, $q, $rootScope, $templateCache, $injector , $window) {

        return {
            request: function (config) {
                if (config) {
                    if(config.url.indexOf("api") >= 0){
                        config.headers = config.headers || {};
                        config.headers.Authorization = 'Bearer ' + authService._msal.browserStorage.getItem("msalIdTokenValue" , "Credential");
                    }
                    //Avoid sending requests when not logged in
                    if(!authService.isAuthenticated() && config.url === "/App/Views/TodoList.html"){
                        return $q.reject(config);
                    }
                    return config;

                }
            }
        };
    }]);
