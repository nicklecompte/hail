from hail.utils.java import Env
from hail.ir import *
from hail.expr.types import *
from hail.expr.expressions import *
from hail.typecheck import *

@typecheck(f=anytype, param_types=HailType)
def define_function(f, *param_types):
    mname = Env.get_uid()
    param_names = [Env.get_uid() for _ in param_types]
    body = f(*(construct_expr(Ref(pn), pt) for pn, pt in zip(param_names, param_types)))
    ret_type = body.dtype

    r = Renderer(stop_at_jir=True)
    code = r(body._ir)
    jbody = body._ir.parse(code, ref_map=dict(zip(param_names, param_types)), ir_map=r.jirs)

    Env.hail().expr.ir.functions.IRFunctionRegistry.pyRegisterIR(
        mname, param_names, [pt._parsable_string() for pt in param_types], ret_type._parsable_string(),
        jbody)
    register_function(mname, param_types, ret_type)

    @typecheck(args=expr_any)
    def f(*args):
        indices, aggregations = unify_all(*args)
        return construct_expr(Apply(mname, *(a._ir for a in args)), ret_type, indices, aggregations)
        
    return f