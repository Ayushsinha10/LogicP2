package parser.ast;

import java.util.ArrayList;
import java.util.List;

import parser.ast.FPTerm;

public class FPHead {
  public final String name;
  public final ArrayList<FPTerm> params;

  public FPHead(String n) {
    this.name = n;
    this.params = new ArrayList<FPTerm>();
  }

  public FPHead(String n, ArrayList<FPTerm> ps) {
    this.name = n;
    this.params = ps;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder(name);
    if (params.size() != 0) {
      sb.append("(");
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) { sb.append(", "); };
        sb.append(params.get(i));
      }
      return sb.append(")").toString();
    } else {
      return sb.toString();
    }
  }

public String getName() {
    return name;
}

public List<FPTerm> getTerms() {
    return params;
}
public FPTerm convertToTerm() {
  // Convert the FPHead's name and parameters into an FPTerm
  return new FPTerm(TKind.CTERM, name, new ArrayList<>(params));
}

}