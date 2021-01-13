'use strict';
angular.module('todoApp')
    .controller('todoListCtrl', ['$scope', '$location', 'todoListSvc', 'msalAuthenticationService', function ($scope, $location, todoListSvc, msalService) {
        $scope.error = "";
        $scope.loadingMessage = "";
        $scope.todoList = null;
        $scope.editingInProgress = false;
        $scope.newTodoCaption = "";


        $scope.editInProgressTodo = {
            Description: "",
            ID: 0,
            Owner: msalService.userInfo.userName
        };


        $scope.editSwitch = function (todo) {
            todo.edit = !todo.edit;
            if (todo.edit) {
                $scope.editInProgressTodo.Description = todo.Description;
                $scope.editInProgressTodo.ID = todo.ID;
                $scope.editingInProgress = true;
            } else {
                $scope.editingInProgress = false;
            }
        };

        $scope.populate = function () {
            todoListSvc.getItems().success(function (results) {
                $scope.todoList = results;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = "";
            })
        };
        $scope.delete = function (id) {
            todoListSvc.deleteItem(id).success(function (results) {
                $scope.populate();
                $scope.loadingMessage = "deleteItem: " + results;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = "";
            })
        };
        $scope.update = function (todo) {
            todoListSvc.putItem($scope.editInProgressTodo).success(function (results) {
                $scope.populate();
                $scope.editSwitch(todo);
                $scope.loadingMessage = "putItem: " + results;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = "";
            })
        };
        $scope.add = function () {
            todoListSvc.postItem({
                'Description': $scope.newTodoCaption,
                'Owner': msalService.userInfo.userName
            }).success(function (results) {
                $scope.newTodoCaption = "";
                $scope.populate();
                $scope.loadingMessage = "postItem: " + results;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMsg = "";
            })
        };
    }]);
