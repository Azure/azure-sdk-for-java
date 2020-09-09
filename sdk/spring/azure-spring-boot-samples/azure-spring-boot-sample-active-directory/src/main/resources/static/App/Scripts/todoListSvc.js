'use strict';
angular.module('todoApp')
    .factory('todoListSvc', ['$http', function ($http) {
        return {
            getItems: function () {
                return $http.get('/api/todolist');
            },
            getItem: function (id) {
                return $http.get('/api/todolist/' + id);
            },
            postItem: function (item) {
                return $http.post('/api/todolist/', item);
            },
            putItem: function (item) {
                return $http.put('/api/todolist/', item);
            },
            deleteItem: function (id) {
                return $http({
                    method: 'DELETE',
                    url: '/api/todolist/' + id
                });
            }
        };
    }]);