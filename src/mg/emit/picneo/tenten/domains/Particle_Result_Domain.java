package mg.emit.picneo.tenten.domains;

public class Particle_Result_Domain {

	private Integer id;
	
	private Double width;
	
	private Double height;
	
	private Double area;
	
	private Double x;
	
	private Double y;

	public Particle_Result_Domain(Integer id, Double width, Double height, Double area,
			Double x, Double y) {
		super();
		this.id = id;
		this.width = width;
		this.height = height;
		this.area = area;
		this.x = x;
		this.y = y;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Double getArea() {
		return area;
	}

	public void setArea(Double area) {
		this.area = area;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}
}
