package guru.springframework.controllers;

import guru.springframework.domain.Course;
import guru.springframework.domain.User;
import guru.springframework.repositories.CourseRepository;
import guru.springframework.repositories.UserRepository;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;



// TODO: Auto-generated Javadoc
/**
 * The Class CourseController.
 */
@RestController
public class CourseController {
	
	
	/** The Course service. */
	private CourseRepository CourseService ; //Service which will do all data retrieval/manipulation work
	
	/** The user service. */
	private UserRepository userService;
	
	/**
	 * Instantiates a new course controller.
	 *
	 * @param CourseService the course service
	 * @param userService the user service
	 */
	@Autowired
	public CourseController(CourseRepository CourseService , UserRepository userService) {
		this.CourseService = CourseService;
		this.userService = userService;
	}
	
	
    
   //-------------------Retrieve All Courses--------------------------------------------------------
    
   /**
    * List all courses.
    *
    * @return the response entity
    */
   @RequestMapping(value = "/course/", method = RequestMethod.GET)
   public ResponseEntity<List<Course>> listAllCourses() {
       List<Course> courses = CourseService.findAll();
       UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       System.out.println(userDetails.getUsername());
       if(courses.isEmpty()){
           return new ResponseEntity<List<Course>>(HttpStatus.NO_CONTENT);//You many decide to return HttpStatus.NOT_FOUND
       }
       return new ResponseEntity<List<Course>>(courses, HttpStatus.OK);
   }
   
   

   //-------------------Retrieve Single Course--------------------------------------------------------
    
   /**
    * Gets the course.
    *
    * @param id the id
    * @return the course
    */
   @RequestMapping(value = "/course/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Course> getCourse(@PathVariable("id") long id) {
       System.out.println("Fetching Course with ID " + id);
       Course course = CourseService.findOne(id);
       if (course == null) {
           System.out.println("Course with ID " + id + " Not Found");
           return new ResponseEntity<Course>(HttpStatus.NOT_FOUND);
       }
       return new ResponseEntity<Course>(course, HttpStatus.OK);
   }

   //-------------------Retrieve Resisted Course--------------------------------------------------------
   
   /**
    * Gets the courses registedin.
    *
    * @return the courses registedin
    */
   @RequestMapping(value = "/courseRegistedIn/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<List<Course>> getCoursesRegistedin() {
       System.out.println("Fetching Registed ");
       UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       User user = userService.findByuserName(userDetails.getUsername());
       System.out.println(user.getCoursesRegistedin());
       return new ResponseEntity<List<Course>>(user.getCoursesRegistedin(), HttpStatus.OK);
   }

/**
 * Gets the courses created.
 *
 * @return the courses created
 */
//-------------------Retrieve Created Course--------------------------------------------------------
   @PreAuthorize("hasRole('ROLE_TEACHER')")
   @RequestMapping(value = "/courseCreated/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<List<Course>> getCoursesCreated() {
	   System.out.println("Fetching Created ");
       UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       User user = userService.findByuserName(userDetails.getUsername());
       return new ResponseEntity<List<Course>>(user.getCoursesCreated(), HttpStatus.OK);
   }
    
   
   
   //-------------------Registed a Course--------------------------------------------------------
   
   /**
    * Creates the course.
    *
    * @param userId the user id
    * @param courseId the course id
    * @return the response entity
    */
   @RequestMapping(value = "/course/{userId}/{courseId}", method = RequestMethod.GET)
   public ResponseEntity<Void> RegistedIn(@PathVariable("userId") long userId,@PathVariable("courseId") long courseId) {
	   Course course = CourseService.findOne(courseId);
       if ( course == null  ) {
           System.out.println("A Course with name " + courseId + " already exist");
           return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
       }
       User user = userService.findOne(userId) ;
       if(user == null){
    	   System.out.println("A User with id " + userId + " was not found");
    	   return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
       }
       user.addCoursesRegistedin(course);
       userService.save(user);
       
       return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
   }
   //-------------------Create a Course--------------------------------------------------------
    
   /**
    * Creates the course.
    *
    * @param userId the user id
    * @param course the course
    * @param ucBuilder the uc builder
    * @return the response entity
    */
   @PreAuthorize("hasRole('ROLE_TEACHER')")
   @RequestMapping(value = "/course/{userId}", method = RequestMethod.POST)
   public ResponseEntity<Void> createCourse(@PathVariable("userId") long userId,@RequestBody Course course,    UriComponentsBuilder ucBuilder) {
       System.out.println("Creating Course " + course.getName());
       if ( CourseService.findByname(course.getName()) != null  ) {
           System.out.println("A Course with name " + course.getName() + " already exist");
           return new ResponseEntity<Void>(HttpStatus.CONFLICT);
       }
       User user = userService.findOne(userId);
       
       if(user == null){
    	   System.out.println("A User with id " + userId + " was not found");
    	   return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
       }
       
       user.addCourses(course);
       CourseService.save(course);

       HttpHeaders headers = new HttpHeaders();
       headers.setLocation(ucBuilder.path("/course/{id}").buildAndExpand(course.getId()).toUri());
       return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
   }

    
   //------------------- Update a Course--------------------------------------------------------
   
   /**
    * Update course.
    *
    * @param id the id
    * @param course the course
    * @return the response entity
    */
   @PreAuthorize("hasRole('ROLE_TEACHER')")
   @RequestMapping(value = "/course/{id}", method = RequestMethod.PUT)
   public ResponseEntity<Course> updateCourse(@PathVariable("id") long id, @RequestBody Course course) {
       System.out.println("Updating Coures " + id);
       Course currentCourse = CourseService.findOne(id);
       UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       if (currentCourse == null) {
           System.out.println("Course with ID " + id + " not found");
           return new ResponseEntity<Course>(HttpStatus.NOT_FOUND);
       }else if(!currentCourse.getTeacher().getUserName().equals(userDetails.getUsername())){
    	   System.out.println("tring to update Course not belonging to the user ");
    	   return new ResponseEntity<Course>(HttpStatus.NOT_ACCEPTABLE);
    	   
       }
       
       currentCourse.setName(course.getName());
       currentCourse.setDescption(course.getDescption());
       currentCourse.setImageSrc(course.getImageSrc());
       
       
       CourseService.save(currentCourse);
       return new ResponseEntity<Course>(currentCourse, HttpStatus.OK);
   }

   //------------------- Delete a Course --------------------------------------------------------
   
   /**
    * Delete course.
    *
    * @param id the id
    * @return the response entity
    */
   @PreAuthorize("hasRole('ROLE_TEACHER')")
   @RequestMapping(value = "/course/{id}", method = RequestMethod.DELETE)
   public ResponseEntity<Course> deleteCourse(@PathVariable("id") long id) {
	   
       System.out.println("Fetching & Deleting Course with ID " + id);
       UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       Course course = CourseService.findOne(id);
       if (course == null) {
           System.out.println("Unable to delete. Course with ID " + id + " not found");
           return new ResponseEntity<Course>(HttpStatus.NOT_FOUND);
       }else if(!course.getTeacher().getUserName().equals(userDetails.getUsername())){
    	   System.out.println("tring to update Course not belonging to the user ");
    	   return new ResponseEntity<Course>(HttpStatus.NOT_ACCEPTABLE);
    	   
       }

       CourseService.delete(id);
       return new ResponseEntity<Course>(HttpStatus.NO_CONTENT);
   }
	
	

}
