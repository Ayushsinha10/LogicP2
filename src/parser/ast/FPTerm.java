package parser.ast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import parser.ast.TKind;

public class FPTerm {
  public final TKind kind;
  public final String name;
  public final ArrayList<FPTerm> args;

  public FPTerm() {
    this.kind = TKind.BANG;
    this.name = "!";
    this.args = null;
  }

  public FPTerm(FPTerm t) {
    this.kind = TKind.NOT;
    this.name = "\\+";
    this.args = new ArrayList<FPTerm>();
    this.args.add(t);
  }

  public FPTerm(TKind k, String n) {
    this.kind = k;
    this.name = n;
    this.args = null;
  }

  public FPTerm(TKind k, String n, ArrayList<FPTerm> as) {
    this.kind = k;
    this.name = n;
    this.args = as;
  }


  public String toString() {
    if (this.kind == TKind.CONST || this.kind == TKind.CTERM) {
      StringBuilder sb = new StringBuilder(name);
      if (args != null && args.size() > 0) {
        sb.append("(");
        for (int i = 0; i < args.size(); i++) {
          if (i > 0) { sb.append(", "); };
          sb.append(args.get(i));
        }
        return sb.append(")").toString();
      } else {
        return sb.toString();
      }
    } else if (this.kind == TKind.NOT) {
      return name + " " + args.get(0);
    } else {
      return name;
    }
  }

  public TKind getKind() {
     return kind;
  }

public String getName() {
    return name;
}

public ArrayList<FPTerm> getTerms() {
 return args;
}
    @Override
    public boolean equals(Object obj) {
        // Check for self-equality
        if (this == obj) {
            return true;
        }

        // Check if the object is of the same class
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        FPTerm other = (FPTerm) obj;

        // Check if the kind, name, and arguments are equal
        return kind == other.kind &&
               Objects.equals(name, other.name) &&
               Objects.equals(args, other.args);
    }
}