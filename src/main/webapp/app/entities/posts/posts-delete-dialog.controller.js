(function() {
    'use strict';

    angular
        .module('blogisterApp')
        .controller('PostsDeleteController',PostsDeleteController);

    PostsDeleteController.$inject = ['$uibModalInstance', 'entity', 'Posts'];

    function PostsDeleteController($uibModalInstance, entity, Posts) {
        var vm = this;

        vm.posts = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Posts.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
