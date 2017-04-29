 app.service('GamesService', ['$http', function ($http) {

        var mcqBase = 'http://localhost:8080/mcqgame/';
        var tfBase = 'http://localhost:8080/tfgame/';
        var user = 'http://localhost:8080/user/';
        var selectedGameToPlay ;
        var selectedGameToEdit ;
        var gameScore;
        
      
        this.getGame = function (id , type) {
        	if(type == "MCQ")return $http.get(mcqBase + id);
        	else if(type == "TF" ) return $http.get(tfBase + id);
        };
        
        
        this.setSelectedGameToPlay = function(data){
        	return this.selectedGameToPlay = data;
        }
        
        this.getSelectedGameToPlay = function(){
        	return this.selectedGameToPlay;
        }
        
        this.setSelectedGameToEdit = function(data){
        	return this.selectedGameToEdit = data;
        }
        
        this.getSelectedGameToEdit = function(){
        	return this.selectedGameToEdit;
        }
        
        this.setGameScore = function(data,userId,gameId){
        	$http.get(user +userId + '/' +gameId+'/'+data.scoreFront );
        	return this.gameScore = data;
        }

        this.getGameScore = function(data){
        	return this.gameScore;
        }
        
        this.insertGame = function (id,data ,type) {
        	if(type == "MCQ")return $http.post(mcqBase + id,data);
        	else if(type == "TF" ) return $http.post(tfBase + id,data);
        };
//
//        this.updateCustomer = function (cust) {
//            return $http.put(urlBase + '/' + cust.ID, cust)
//        };
//
//        this.deleteCustomer = function (id) {
//            return $http.delete(urlBase + '/' + id);
//        };
//
//        this.getOrders = function (id) {
//            return $http.get(urlBase + '/' + id + '/orders');
//        };
    }]);