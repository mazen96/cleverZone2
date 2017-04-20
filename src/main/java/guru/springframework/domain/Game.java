package guru.springframework.domain;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

@MappedSuperclass
public abstract class Game extends AbstractContent {
	//write the common things between games
	@NotNull
	@NotEmpty
	protected String name;
	@NotNull
	@NotEmpty
	private String descption;
	@NotNull
	@NotEmpty
	private String imageSrc;
	@NotNull
	private int totalTime;
	

	

	public Game(){
		super();
		name = descption = imageSrc = null;
		totalTime = 0;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getdescption() {
		return descption;
	}
	public void setdescption(String desc) {
		this.descption = desc;
	}
	public String getImageSrc() {
		return imageSrc;
	}
	public void setImageSrc(String imageSrc) {
		this.imageSrc = imageSrc;
	}
	public int getTotalTime() {
		return totalTime;
	}

	public void addTotalTime(int totalTime) {
		this.totalTime += totalTime;
	}
	
}
