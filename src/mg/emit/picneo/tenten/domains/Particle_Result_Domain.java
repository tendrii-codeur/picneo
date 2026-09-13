package mg.emit.picneo.tenten.domains;

public class Particle_Result_Domain {

	private Integer id;
	
	private Double width;
	
	private Double height;
	
	private Double area;
	
	private Double mean;
	
	private Double min;
	
	private Double max;
	
	private Double x;
	
	private Double y;
	
	private Double int_den;
	
	private Double raw_int_den;

	public Particle_Result_Domain(Integer id, Double width, Double height, Double area, Double mean, Double min,
			Double max, Double x, Double y, Double int_den, Double raw_int_den) {
		super();
		this.id = id;
		this.width = width;
		this.height = height;
		this.area = area;
		this.mean = mean;
		this.min = min;
		this.max = max;
		this.x = x;
		this.y = y;
		this.int_den = int_den;
		this.raw_int_den = raw_int_den;
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

	public Double getMean() {
		return mean;
	}

	public void setMean(Double mean) {
		this.mean = mean;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
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

	public Double getInt_den() {
		return int_den;
	}

	public void setInt_den(Double int_den) {
		this.int_den = int_den;
	}

	public Double getRaw_int_den() {
		return raw_int_den;
	}

	public void setRaw_int_den(Double raw_int_den) {
		this.raw_int_den = raw_int_den;
	}
}
