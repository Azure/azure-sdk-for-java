angular.module('MsalAngular', [])
    .provider('msalAuthenticationService', function () {
        var _msal = null;
        var _config = null;
        var _constants = null;
        var _oauthData = { isAuthenticated: false, userName: '', loginError: '', idToken: {} };

        var updateDataFromCache = function (scopes) {
            // only cache lookup here to not interrupt with events

            var account =  _msal.browserStorage.getItem("msalAccount" , "Account");
            var idTokenClaims =  _msal.browserStorage.getItem("msalIdToken" , "Credential");
            var idToken =  _msal.browserStorage.getItem("msalIdTokenValue" , "Credential");

            _oauthData.isAuthenticated = idTokenClaims != null && account !== null && idToken.length > 0;
            if(account){
                _oauthData.userName = account.username;
                _oauthData.idToken = idTokenClaims;
            }

        };
        var  handleResponse = function(resp) {
            if (resp !== null) {
                _oauthData.idToken = resp.idTokenClaims;

                _msal.browserStorage.setItem("msalAccount" , resp.account , "Account");
                _msal.browserStorage.setItem("msalIdToken" , resp.idTokenClaims , "Credential");
                _msal.browserStorage.setItem("msalIdTokenValue" , resp.idToken , "Credential");

                _oauthData.isAuthenticated = true;
                _oauthData.userName = resp.account.username;

            }
            else {
                /**
                 * See here for more info on account retrieval:
                 * https://github.com/AzureAD/microsoft-authentication-library-for-js/blob/dev/lib/msal-common/docs/Accounts.md
                 */
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

            _msal.handleRedirectPromise().then(handleResponse).catch(err => {
                console.error(err);
            });
            // loginResource is used to set authenticated status
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

                $timeout(function () {
                    updateDataFromCache(_msal.loginScopes);
                    $rootScope.userInfo = _oauthData;
                }, 1);
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

            function getStates(toState) {
                var state = null;
                var states = [];
                if (toState.hasOwnProperty('parent')) {
                    state = toState;
                    while (state) {
                        states.unshift(state);
                        state = $injector.get('$state').get(state.parent);
                    }
                }
                else {
                    var stateNames = toState.name.split('.');
                    for (var i = 0, stateName = stateNames[0]; i < stateNames.length; i++) {
                        state = $injector.get('$state').get(stateName);
                        if (state) {
                            states.push(state);
                        }
                        stateName += '.' + stateNames[i + 1];
                    }
                }
                return states;
            }

            var routeChangeHandler = function (e, nextRoute) {
                if (nextRoute && nextRoute.$$route) {
                    var requireLogin = _msal.routeProtectionConfig.requireLogin || nextRoute.$$route.requireLogin;
                    if (requireLogin) {
                        if (!_oauthData.isAuthenticated) {
                            if (!_msal.interactionInProgress()) {
                                // _msal._logger.info('Route change event for:' + $location.$$url);
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

            var stateChangeHandler = function (e, toState, toParams, fromState, fromParams) {
                if (toState) {
                    var states = getStates(toState);
                    var state = null;
                    for (var i = 0; i < states.length; i++) {
                        state = states[i];
                        var requireLogin = _msal.routeProtectionConfig.requireLogin || state.requireLogin;
                        if (requireLogin) {
                            if (!_oauthData.isAuthenticated) {
                                if (!_msal._renewActive && !_msal.getUser()) {
                                    _msal._logger.info('State change event for:' + $location.$$url);
                                    var $state = $injector.get('$state');
                                    var loginStartPage = $state.href(toState, toParams, { absolute: true });
                                    loginHandler(loginStartPage, _msal.routeProtectionConfig);
                                }
                            }
                        }
                        else if (state.templateUrl) {
                            var nextStateUrl;
                            if (typeof state.templateUrl === 'function') {
                                nextStateUrl = state.templateUrl(toParams);
                            }
                            else {
                                nextStateUrl = state.templateUrl;
                            }
                            if (nextStateUrl && !isUnprotectedResource(nextStateUrl)) {
                                _msal._unprotectedResources.push(nextStateUrl);
                            }
                        }
                    }
                }
            };

            var stateChangeErrorHandler = function (event, toState, toParams, fromState, fromParams, error) {
                _msal._logger.verbose("State change error occured. Error: " + typeof (error) === 'string' ? error : JSON.stringify(error));
                // msal interceptor sets the error on config.data property. If it is set, it means state change is rejected by msal,
                // in which case set the defaultPrevented to true to avoid url update as that sometimesleads to infinte loop.
                if (error && error.data) {
                    _msal._logger.info("Setting defaultPrevented to true if state change error occured because msal rejected a request. Error: " + error.data);
                    if (event)
                        event.preventDefault();
                }
            };

            if ($injector.has('$transitions')) {
                var $transitions = $injector.get('$transitions');

                function onStartStateChangeHandler(transition) {
                    stateChangeHandler(null, transition.to(), transition.params('to'), transition.from(), transition.params('from'));
                }

                function onErrorStateChangeHandler(transition) {
                    stateChangeErrorHandler(null, transition.to(), transition.params('to'), transition.from(), transition.params('from'), transition.error());
                }

                $transitions.onStart({}, onStartStateChangeHandler);
                $transitions.onError({}, onErrorStateChangeHandler);
            }

            // Route change event tracking to receive fragment and also auto renew tokens
            $rootScope.$on('$routeChangeStart', routeChangeHandler);

            $rootScope.$on('$stateChangeStart', stateChangeHandler);

            $rootScope.$on('$locationChangeStart', locationChangeHandler);

            $rootScope.$on('$stateChangeError', stateChangeErrorHandler);


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

                userInfo: _oauthData,
            };
        }];
    });

// Interceptor for http if needed
angular.module('MsalAngular')
    .factory('ProtectedResourceInterceptor', ['msalAuthenticationService', '$q', '$rootScope', '$templateCache', '$injector', function (authService, $q, $rootScope, $templateCache, $injector) {

        return {
            request: function (config) {
                if (config) {

                    if(config.url.indexOf("api") >= 0){
                        config.headers = config.headers || {};
                        config.headers.Authorization = 'Bearer ' + authService._msal.browserStorage.getItem("msalIdTokenValue" , "Credential");
                    }
                    if(!authService.isAuthenticated() && config.url === "/App/Views/TodoList.html"){
                        return $q.reject(config);
                    }

                    return config;

                }
            }
        };
    }]);
