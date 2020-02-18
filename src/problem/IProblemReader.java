package problem;

public interface IProblemReader {
	public IProblemInstance readProblem(String problemName) throws ProblemReadingException;
}
