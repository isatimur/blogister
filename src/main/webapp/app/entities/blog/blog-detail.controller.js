(function() {
    'use strict';

    angular
        .module('blogisterApp')
        .controller('BlogDetailController', BlogDetailController);

    BlogDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'Blog', 'User', 'Posts'];

    function BlogDetailController($scope, $rootScope, $stateParams, entity, Blog, User, Posts) {
        var vm = this;

        vm.blog = entity;

        var unsubscribe = $rootScope.$on('blogisterApp:blogUpdate', function(event, result) {
            vm.blog = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
