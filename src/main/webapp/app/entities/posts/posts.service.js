(function() {
    'use strict';
    angular
        .module('blogisterApp')
        .factory('Posts', Posts);

    Posts.$inject = ['$resource', 'DateUtils'];

    function Posts ($resource, DateUtils) {
        var resourceUrl =  'api/posts/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.creattionDate = DateUtils.convertDateTimeFromServer(data.creattionDate);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
