(function() {
    'use strict';

    angular
        .module('blogisterApp')
        .controller('PostsDetailController', PostsDetailController);

    PostsDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'Posts', 'Tag', 'Blog'];

    function PostsDetailController($scope, $rootScope, $stateParams, entity, Posts, Tag, Blog) {
        var vm = this;

        vm.posts = entity;

        var unsubscribe = $rootScope.$on('blogisterApp:postsUpdate', function(event, result) {
            vm.posts = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
