package guru.springframework.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import guru.springframework.repositories.CourseRepository;
import guru.springframework.repositories.MCQGameRepository;
import guru.springframework.repositories.NewGameNotificationRepository;
import guru.springframework.repositories.UserRepository;
import guru.springframework.domain.Comment;
import guru.springframework.domain.Course;
import guru.springframework.domain.MCQ_Game;
import guru.springframework.domain.NewGameNotification;
import guru.springframework.domain.TF_Game;
import guru.springframework.domain.User;

// TODO: Auto-generated Javadoc
/**
 * The Class MCQ_Game_Controller.
 */
@RestController
public class MCQ_Game_Controller {

	/** The MCQ service. */
	MCQGameRepository MCQService;
	
	/** The user service. */
	UserRepository userService;
	
	/** The course service. */
	CourseRepository courseService;
	
	/** The notify service. */
	NewGameNotificationRepository notifyService;
	
	/**
	 * Instantiates a new MC Q game controller.
	 *
	 * @param mcqgame the mcqgame
	 * @param userService the user service
	 * @param courseService the course service
	 * @param notifyService the notify service
	 */
	@Autowired
    public MCQ_Game_Controller(MCQGameRepository mcqgame, UserRepository userService,
			CourseRepository courseService,NewGameNotificationRepository notifyService) {
		this.MCQService = mcqgame;
		this.userService = userService;
		this.courseService = courseService;
		this.notifyService =notifyService;
	}
	
	/**
	 * Notify users.
	 *
	 * @param courseId the course id
	 * @param gameId the game id
	 */
	private void NotifyUsers(Long courseId,Long gameId){
		Course course =  courseService.getOne(courseId);
		List<User> users = course.getUsers();
		List<NewGameNotification> notifys = new ArrayList<NewGameNotification>();
		for(int i=0;i<users.size();i++){
			NewGameNotification tmp = new NewGameNotification(users.get(i), course.getName(),gameId );
			users.get(i).addNotifications(tmp);
			notifys.add(tmp);
		}
		notifyService.save(notifys);
		return;
	}
	
    /**
     * Checks if is owner.
     *
     * @param user the user
     * @return true, if is owner
     */
    private boolean isOwner(User user){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUserName().equals(userDetails.getUsername());
    }
    
    /**
     * Checks if is collaborator.
     *
     * @param game the game
     * @return true, if is collaborator
     */
    private boolean isCollaborator(MCQ_Game game){
    	boolean valid=false;
    	UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        for(User user : game.getCollaborators()){
        	 valid|=user.getUserName().equals(userDetails.getUsername());
        }
        return valid;
    }
    
    /**
     * Cancel game.
     *
     * @param gameId the game id
     * @param state the state
     * @return the response entity
     */
    //-------------------Cancel specific mcq game -------------------------
    @RequestMapping(value = "/cancelmcqgame/{gameId}/{state}", method = RequestMethod.POST)
    public ResponseEntity<Void> cancelGame(@PathVariable("gameId") long gameId , @PathVariable("state") boolean state) {
    	MCQ_Game game = MCQService.findOne(gameId);
        if (game == null) {
            System.out.println("no game  found");
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }
        game.setCancled(state);
        MCQService.save(game);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
    
	/**
	 * Gets the comment.
	 *
	 * @param gameId the game id
	 * @return the comment
	 */
	//-------------------Retrieve all mcqGame Comments  -------------------------
    @RequestMapping(value = "/mcqgamecomment/{gameId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<	List<Comment> > getcomment(@PathVariable("gameId") long gameId) {
        System.out.println("Fetching all mcq Game comments ");
        MCQ_Game game = MCQService.findOne(gameId);
        if (game == null) {
            System.out.println("no game  found");
            return new ResponseEntity<List<Comment >>(HttpStatus.NOT_FOUND);
        }
        List<Comment>comments = game.getComments();
        System.out.println(comments.size());
        for(Comment c  : comments){
        	System.out.println(c.getText());
        }
        return new ResponseEntity<List<Comment > >(comments,HttpStatus.OK);
    }
 
	/**
	 * Gets the game.
	 *
	 * @return the game
	 */
	//-------------------Retrieve all MCQ Game -------------------------
    @RequestMapping(value = "/mcqgame/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MCQ_Game > > getGame() {
        System.out.println("Fetching all mcq Games");
        List<MCQ_Game > game = (ArrayList<MCQ_Game>) MCQService.findAll();
        if (game == null) {
            System.out.println("no game  found");
            return new ResponseEntity<List<MCQ_Game >>(HttpStatus.NOT_FOUND);
        }
        
        for(int i = 0 ; i < game.size() ; i++)
        {
        	if(game.get(i).isCancled() == true)
        	{
        		game.remove(i);
        	}
        }
        
        return new ResponseEntity<List<MCQ_Game > >(game,HttpStatus.OK);
    }
    //-------------------Retrieve MCQ Game ------------------------------
    
    /**
     * Gets the game.
     *
     * @param id the id
     * @return the game
     */
    @RequestMapping(value = "/mcqgame/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MCQ_Game> getGame(@PathVariable("id") long id) {
        System.out.println("Fetching mcqGame with id " + id);
        MCQ_Game game = MCQService.findOne(id);
        if (game == null) {
            System.out.println("game with id " + id + " not found");
            return new ResponseEntity<MCQ_Game>(HttpStatus.NOT_FOUND);
        }
        
        if(game.isCancled() == true)
        {
        	return new ResponseEntity<MCQ_Game>(HttpStatus.CONFLICT);
        }
        
        return new ResponseEntity<MCQ_Game>(game, HttpStatus.OK);
    }
    


	//-------------------Create a MCQ game-------------------------------------

	/**
	 * Creates the MCQ game.
	 *
	 * @param courseId the course id
	 * @param Game the game
	 * @param ucBuilder the uc builder
	 * @return the response entity
	 */
	@PreAuthorize("hasRole('ROLE_TEACHER')")
    @RequestMapping(value = "/mcqgame/{courseId}", method = RequestMethod.POST)
    public ResponseEntity<Void> createMCQGame(@PathVariable("courseId") long courseId,@RequestBody MCQ_Game Game,    UriComponentsBuilder ucBuilder) {
        System.out.println("Creating mcq Game " + Game.getName());
        if ( MCQService.findByname(Game.getName()) != null  ) {
            System.out.println("A MCQ Game with name " + Game.getName() + " already exist");
            return new ResponseEntity<Void>(HttpStatus.CONFLICT);
        }
        Course course=courseService.findOne(courseId);
        if(course==null){
            System.out.println("Course with ID " + courseId + " Not Found");
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }
        User user = course.getTeacher();
        if(!isOwner(user)){
     	   System.out.println("tring to create a game to a Course not belonging to the user ");
     	   return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
        }
        course.addContents(Game);
        Game.setType("MCQ");
        MCQService.save(Game);
        NotifyUsers(courseId,Game.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/course/{id}").buildAndExpand(course.getId()).toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }
	
	//-------------------Copy MCQ game-------------------------------------

		/**
	 * Copy MCQ game.
	 *
	 * @param courseId the course id
	 * @param gameId the game id
	 * @return the response entity
	 */
	@PreAuthorize("hasRole('ROLE_TEACHER')")
	    @RequestMapping(value = "/mcqgame/{courseId}/{gameId}", method = RequestMethod.GET)
	    public ResponseEntity<Void> copyMCQGame(@PathVariable("courseId") long courseId,@PathVariable("gameId") long gameId) {
	        //System.out.println("Copying mcq Game " + gameId.getName());
	        
	        MCQ_Game Game = MCQService.findOne(gameId);
	        if (Game == null){
	        	System.out.println("Game with ID " + gameId + " Not Found");
	        	return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
	        }
	        
	        Course course = courseService.findOne(courseId);
	        if(course==null){
	            System.out.println("Course with ID " + courseId + " Not Found");
	            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
	        }
	        
	        User user = course.getTeacher();
	        if(!isOwner(user)){
	     	   System.out.println("trying to copy a game to a Course not belonging to the user ");
	     	   return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
	        }
	        
	        MCQ_Game gameCopy = new MCQ_Game();
  	        gameCopy.setName(Game.getName() + "-" + getSaltString());
  	        gameCopy.setdescption(Game.getdescption());
  	        gameCopy.setImageSrc(Game.getImageSrc());
  	        gameCopy.setType(Game.getType());
  	        gameCopy.setCourse(course);
  	        gameCopy.setCancled(false);
  	        gameCopy.setTotalTime(Game.getTotalTime());
  	        for(int i = 0 ; i < Game.getQuestions().size(); i++)
  	        {
  	        	gameCopy.addQuestion(Game.getQuestions().get(i));
  	        }
	        course.addContents(gameCopy);
	        MCQService.save(gameCopy);
	        NotifyUsers(courseId,gameCopy.getId());
	        return new ResponseEntity<Void>(HttpStatus.CREATED);
	    }
    
    //------------------- Delete a mcq game --------------------------------
    
    /**
     * Delete game.
     *
     * @param id the id
     * @return the response entity
     */
    @RequestMapping(value = "/mcqgame/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<MCQ_Game> deleteGame(@PathVariable("id") long id) {
    	MCQ_Game game = MCQService.findOne(id);
        if (game == null) {
            System.out.println("Unable to delete. game with id " + id + " not found");
            return new ResponseEntity<MCQ_Game>(HttpStatus.NOT_FOUND);
        }
        User user = game.getCourse().getTeacher();
        if(!isOwner(user)){
     	   System.out.println("tring to delete a game to a Course not belonging to the user ");
     	   return new ResponseEntity<MCQ_Game>(HttpStatus.NOT_ACCEPTABLE);
        }
        MCQService.delete(id);
        return new ResponseEntity<MCQ_Game>(HttpStatus.NO_CONTENT);
    }
    
    
    //------------------- Update a mcq Game --------------------------------------------------------
    
    /**
     * Update game.
     *
     * @param id the id
     * @param game the game
     * @return the response entity
     */
    @RequestMapping(value = "/mcqgame/{id}", method = RequestMethod.PUT)
    public ResponseEntity<MCQ_Game> updateGame(@PathVariable("id") long id, @RequestBody MCQ_Game game) {
        System.out.println("Updating Game " + id);
        MCQ_Game currentGame = MCQService.findOne(id);
        if (currentGame==null) {
            System.out.println("Game with id " + id + " not found");
            return new ResponseEntity<MCQ_Game>(HttpStatus.NOT_FOUND);
        }
        User user = currentGame.getCourse().getTeacher();
        if(!isOwner(user) && !isCollaborator(currentGame)){
     	   System.out.println("tring to update a game to a Course not belonging to the user ");
     	   return new ResponseEntity<MCQ_Game>(HttpStatus.NOT_ACCEPTABLE);
        }
        currentGame.setCourse(currentGame.getCourse());
        currentGame.setName(game.getName());
        currentGame.setdescption(game.getdescption());
        currentGame.setImageSrc(game.getImageSrc());
        currentGame.setTotalTime(game.getTotalTime());
        MCQService.save(currentGame);
        return new ResponseEntity<MCQ_Game>(currentGame, HttpStatus.OK); // ?
    }
    
  /**
   * Gets the salt string.
   *
   * @return the salt string
   */
  //------------------- Utility function --------------------------------------------------------
    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}
