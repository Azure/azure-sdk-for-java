'use strict';
angular.module('todoApp')
    .controller('homeCtrl', ['$scope', 'msalAuthenticationService', '$location', function ($scope, msalService, $location) {
        $scope.login = function () {
            msalService.loginRedirect({
                scopes: ["User.Read"]
            });
        };
        $scope.logout = function () {
            msalService.logout();
        };
        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    }]);
